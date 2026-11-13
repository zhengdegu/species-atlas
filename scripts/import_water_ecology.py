# -*- coding: utf-8 -*-
"""
物种数据导入脚本 - Water Ecology 水生态项目
从 Excel 读取物种数据，通过 API 接口导入到水生态系统
支持批量导入，自动登录获取 Token

用法:
    python import_water_ecology.py                              # 导入所有类型
    python import_water_ecology.py --type AQ                    # 只导入水生植物
    python import_water_ecology.py --dry-run                    # 预览模式
    python import_water_ecology.py --api-url http://localhost:9521/prod-api   # 指定 API 地址
    python import_water_ecology.py -u admin -p admin123         # 指定登录账号密码
"""

import os
import re
import sys
import json
import time
from pathlib import Path
from datetime import datetime

try:
    import pandas as pd
    import requests
except ImportError as e:
    print(f"缺少依赖: {e}")
    print("请运行: pip install pandas openpyxl xlrd requests")
    sys.exit(1)

# ============================================================
# 配置
# ============================================================

# 项目根目录
BASE_DIR = Path(__file__).parent.parent

# API 配置
API_CONFIG = {
    'base_url': 'http://116.131.53.50:9521/prod-api',  # 生产环境
    # 'base_url': 'http://localhost:8080',  # 本地开发
    'username': 'admin',
    'password': 'admin123',
    'timeout': 30,
    'batch_size': 50,  # 每批导入数量
}

# 物种类型配置 - 水生态系统 API
SPECIES_CONFIG = {
    'MA': {
        'name': '底栖动物',
        'api_path': '/system/benthicAnimalDataSummary',
        'excel': '图谱数据/底栖动物/底栖动物.xls',
        'sequence_dir': None,  # 暂无序列目录
        'photo_dir': '图谱数据/底栖动物/常见水生生物图谱-底栖动物',
        'group_code': 'MA',  # 水生生物类群编码
    },
    'AQ': {
        'name': '水生植物',
        'api_path': '/system/aquaticPlantDataSummary',
        'excel': '图谱数据/水生植物/水生植物.xls',
        'sequence_dir': '图谱数据/水生植物/水生植物序列',
        'photo_dir': '图谱数据/水生植物/常见水生生物图谱-水生植物',
        'group_code': 'AQ',
    },
    'PH': {
        'name': '浮游植物',
        'api_path': '/system/phytoplanktonDataSummary',
        'excel': '图谱数据/浮游植物/浮游植物.xls',
        'sequence_dir': '图谱数据/浮游植物/浮游植物序列',
        'photo_dir': '图谱数据/浮游植物/常见水生生物图谱-浮游植物',
        'group_code': 'PH',
    },
    'ZO': {
        'name': '浮游动物',
        'api_path': '/system/zooplanktonDataSummary',
        'excel': '图谱数据/浮游动物/浮游动物.xls',
        'sequence_dir': '图谱数据/浮游动物/浮游动物序列',
        'photo_dir': '图谱数据/浮游动物/常见水生生物图谱-浮游动物',
        'group_code': 'ZO',
    },
    'FI': {
        'name': '鱼类',
        'api_path': '/system/fishDataSummary',
        'excel': '图谱数据/鱼类/鱼类.xlsx',
        'sequence_dir': '图谱数据/鱼类/鱼类序列',
        'photo_dir': '图谱数据/鱼类/常见水生生物图谱-鱼类',
        'group_code': 'FI',
    },
}

# ============================================================
# 工具函数
# ============================================================

def parse_species_name(name):
    """
    拆分物种名称为中文名和拉丁名
    例如: "江西等蜉Isonychia kiangsinensis" -> ("江西等蜉", "Isonychia kiangsinensis")
    """
    if pd.isna(name) or not name:
        return '', ''
    
    name = str(name).strip()
    
    # 移除零宽字符和其他不可见字符
    name = re.sub(r'[\u200b-\u200f\u2028-\u202f\u205f-\u206f]', '', name)
    
    # 匹配中文部分和英文/拉丁名部分
    match = re.match(r'^([\u4e00-\u9fa5\s]+)([A-Za-z].*)$', name)
    if match:
        chinese = match.group(1).strip()
        latin = match.group(2).strip()
        return chinese, latin
    
    # 如果没有匹配到，可能全是中文或全是英文
    if re.match(r'^[\u4e00-\u9fa5\s]+$', name):
        return name, ''
    
    return name, ''


def read_sequence_file(sequence_dir, dna_code):
    """
    读取 DNA 序列文件内容
    """
    if not sequence_dir or not dna_code or pd.isna(dna_code):
        return None
    
    dna_code = str(dna_code).strip()
    if not dna_code or dna_code.lower() == 'nan':
        return None
    
    sequence_path = BASE_DIR / sequence_dir
    if not sequence_path.exists():
        return None
    
    # 尝试多种文件名格式
    possible_names = [
        f'{dna_code}.txt',
        f'{dna_code.replace("-", "_")}.txt',
        f'{dna_code.replace("_", "-")}.txt',
        f'{dna_code.upper()}.txt',
        f'{dna_code.lower()}.txt',
    ]
    
    for filename in possible_names:
        filepath = sequence_path / filename
        if filepath.exists():
            try:
                with open(filepath, 'r', encoding='utf-8') as f:
                    return f.read().strip()
            except Exception as e:
                print(f"    警告: 读取序列文件失败 {filepath}: {e}")
                return None
    
    return None


def find_photo_path(photo_dir, species_code):
    """
    查找物种的照片路径（相对路径字符串）
    水生态系统存储的是路径字符串，不是 JSON 数组
    """
    if not photo_dir or not species_code:
        return None
    
    photo_path = BASE_DIR / photo_dir
    if not photo_path.exists():
        return None
    
    species_code = str(species_code).strip()
    
    # 支持的图片格式
    extensions = ['.jpg', '.jpeg', '.png', '.gif']
    
    # 查找匹配的图片文件
    photos = []
    for ext in extensions:
        for photo_file in photo_path.glob(f"{species_code}-*{ext}"):
            photos.append(photo_file.name)
    
    if photos:
        photos.sort()
        # 返回第一张照片的相对路径
        return f"{photo_dir}/{photos[0]}"
    
    return None


def get_value(row, col_name):
    """
    安全获取 DataFrame 行的值
    """
    if col_name not in row.index:
        return None
    val = row.get(col_name, None)
    if pd.isna(val):
        return None
    val = str(val).strip()
    return val if val and val.lower() != 'nan' else None


def extract_serial_number(species_code):
    """
    从物种编号提取序号
    例如: MA0002 -> 2
    """
    if not species_code:
        return None
    match = re.match(r'^[A-Z]+(\d+)$', str(species_code).upper())
    return int(match.group(1)) if match else None


# ============================================================
# API 操作
# ============================================================

class WaterEcologyAPI:
    """水生态系统 API 客户端"""
    
    def __init__(self, base_url, username, password, timeout=30):
        self.base_url = base_url.rstrip('/')
        self.username = username
        self.password = password
        self.timeout = timeout
        self.token = None
        self.session = requests.Session()
    
    def login(self):
        """登录获取 Token"""
        url = f"{self.base_url}/login"
        data = {
            'username': self.username,
            'password': self.password
        }
        
        try:
            response = self.session.post(url, json=data, timeout=self.timeout)
            result = response.json()
            
            if result.get('code') == 200:
                self.token = result.get('token')
                print(f"登录成功: {self.username}")
                return True
            else:
                print(f"登录失败: {result.get('msg', '未知错误')}")
                return False
        except Exception as e:
            print(f"登录请求失败: {e}")
            return False
    
    def _get_headers(self):
        """获取请求头"""
        headers = {
            'Content-Type': 'application/json',
        }
        if self.token:
            headers['Authorization'] = f'Bearer {self.token}'
        return headers
    
    def batch_insert(self, api_path, data_list):
        """
        批量插入数据
        
        Args:
            api_path: API 路径，如 '/system/phytoplanktonDataSummary'
            data_list: 数据列表
        
        Returns:
            tuple: (success_count, error_message)
        """
        url = f"{self.base_url}{api_path}/batch"
        
        try:
            response = self.session.post(
                url, 
                json=data_list, 
                headers=self._get_headers(),
                timeout=self.timeout
            )
            result = response.json()
            
            if result.get('code') == 200:
                count = result.get('data', len(data_list))
                return count, None
            else:
                return 0, result.get('msg', '未知错误')
        except Exception as e:
            return 0, str(e)
    
    def single_insert(self, api_path, data):
        """
        单条插入数据
        
        Args:
            api_path: API 路径
            data: 单条数据
        
        Returns:
            tuple: (success, error_message)
        """
        url = f"{self.base_url}{api_path}"
        
        try:
            response = self.session.post(
                url, 
                json=data, 
                headers=self._get_headers(),
                timeout=self.timeout
            )
            result = response.json()
            
            if result.get('code') == 200:
                return True, None
            else:
                return False, result.get('msg', '未知错误')
        except Exception as e:
            return False, str(e)
    
    def check_exists(self, api_path, species_code):
        """
        检查记录是否存在
        
        Args:
            api_path: API 路径
            species_code: 物种编码
        
        Returns:
            bool: 是否存在
        """
        url = f"{self.base_url}{api_path}/list"
        params = {
            'speciesCode': species_code,
            'pageNum': 1,
            'pageSize': 1
        }
        
        try:
            response = self.session.get(
                url, 
                params=params,
                headers=self._get_headers(),
                timeout=self.timeout
            )
            result = response.json()
            
            if result.get('code') == 200:
                total = result.get('total', 0)
                return total > 0
            return False
        except:
            return False


# ============================================================
# 导入函数
# ============================================================

def import_species_type(api_client, prefix, dry_run=False, skip_existing=True):
    """
    导入单个物种类型的数据
    
    Args:
        api_client: WaterEcologyAPI 实例
        prefix: 物种前缀 (MA, AQ, PH, ZO, FI)
        dry_run: 是否只预览不实际导入
        skip_existing: 是否跳过已存在的记录
    
    Returns:
        dict: 导入统计结果
    """
    config = SPECIES_CONFIG.get(prefix)
    if not config:
        print(f"错误: 未知的物种前缀 {prefix}")
        return None
    
    print()
    print("=" * 60)
    print(f"导入 {config['name']} ({prefix}) - 水生态系统")
    print("=" * 60)
    print(f"Excel: {config['excel']}")
    print(f"API: {config['api_path']}")
    print(f"序列目录: {config['sequence_dir'] or '(无)'}")
    print()
    
    # 读取 Excel
    excel_path = BASE_DIR / config['excel']
    if not excel_path.exists():
        print(f"错误: Excel 文件不存在 - {excel_path}")
        return None
    
    try:
        # 跳过第一行合并标题，使用第二行作为列名
        df = pd.read_excel(excel_path, header=1)
    except Exception as e:
        print(f"错误: 读取 Excel 失败 - {e}")
        return None
    
    # 过滤有效数据行
    df = df[df['物种编号'].notna()]
    df = df[~df['物种编号'].astype(str).str.contains('说明|第一|第二|展示|级别', na=False)]
    df = df[df['物种编号'].astype(str).str.match(r'^[A-Z]{2}\d+$', na=False)]
    
    print(f"有效数据行数: {len(df)}")
    
    if dry_run:
        print("\n[预览模式] 不会实际调用 API")
    
    # 统计
    stats = {
        'total': len(df),
        'inserted': 0,
        'skipped': 0,
        'errors': []
    }
    
    # 准备数据
    data_batch = []
    
    for idx, row in df.iterrows():
        try:
            species_code = str(row['物种编号']).strip()
            
            # 拆分物种名称
            full_name = get_value(row, '物种名称（中文+拉丁名）')
            chinese_name, latin_name = parse_species_name(full_name)
            
            # 读取 DNA 序列
            dna_code = get_value(row, 'DNA序列编号（可链接到序列信息）')
            dna_sequence = read_sequence_file(config['sequence_dir'], dna_code)
            
            # 查找照片路径
            photo_path = find_photo_path(config['photo_dir'], species_code)
            
            # 提取序号
            serial_number = extract_serial_number(species_code)
            
            # 准备数据 - 符合水生态 DataSummary 实体结构
            data = {
                'serialNumber': serial_number,
                'aquaticOrganismGroupCode': config['group_code'],
                'speciesCode': species_code,
                'chineseName': chinese_name or '',
                'latinName': latin_name or '',
                'taxonomicStatus': get_value(row, '分类学地位') or '',
                'morphologicalDescription': get_value(row, '形态学描述') or '',
                'geographicalDistribution': get_value(row, '地理分布') or '',
                'photoCode': get_value(row, '照片编号') or '',
                'photoPath': photo_path or '',
                'geneName': get_value(row, '基因名称') or '',
                'dnaSequenceCode': dna_code or '',
                'dnaCodingProperty': dna_sequence or '',
                'reference': get_value(row, '数据来源（图书，研究文献，测序，网站）') or '',
            }
            
            if dry_run:
                # 预览模式
                seq_status = "有序列" if dna_sequence else "无序列"
                photo_status = "有照片" if photo_path else "无照片"
                print(f"  {species_code}: {chinese_name} ({latin_name}) [{seq_status}] [{photo_status}]")
                stats['inserted'] += 1
            else:
                data_batch.append(data)
                
        except Exception as e:
            stats['errors'].append({
                'row': idx,
                'species_code': species_code if 'species_code' in dir() else 'unknown',
                'error': str(e)
            })
            print(f"  [错误] 行 {idx}: {e}")
    
    # 批量导入
    if not dry_run and data_batch:
        batch_size = API_CONFIG['batch_size']
        total_batches = (len(data_batch) + batch_size - 1) // batch_size
        
        print(f"\n开始批量导入 ({len(data_batch)} 条数据，{total_batches} 批)...")
        
        for i in range(0, len(data_batch), batch_size):
            batch = data_batch[i:i + batch_size]
            batch_num = i // batch_size + 1
            
            count, error = api_client.batch_insert(config['api_path'], batch)
            
            if error:
                print(f"  批次 {batch_num}/{total_batches}: 失败 - {error}")
                stats['errors'].append({
                    'batch': batch_num,
                    'error': error
                })
            else:
                stats['inserted'] += count
                print(f"  批次 {batch_num}/{total_batches}: 成功导入 {count} 条")
            
            # 避免请求过快
            time.sleep(0.5)
    
    # 打印统计
    print()
    print("-" * 40)
    print(f"导入完成:")
    print(f"  总数: {stats['total']}")
    print(f"  成功: {stats['inserted']}")
    print(f"  跳过: {stats['skipped']}")
    print(f"  错误: {len(stats['errors'])}")
    
    if stats['errors']:
        print("\n错误详情:")
        for err in stats['errors'][:10]:
            if 'row' in err:
                print(f"  行 {err['row']}: {err['error']}")
            elif 'batch' in err:
                print(f"  批次 {err['batch']}: {err['error']}")
    
    return stats


def import_all(api_client, dry_run=False):
    """导入所有物种类型"""
    print("=" * 60)
    print("物种数据导入工具 - 水生态系统")
    print("=" * 60)
    print(f"项目目录: {BASE_DIR}")
    print(f"API 地址: {API_CONFIG['base_url']}")
    print(f"模式: {'预览' if dry_run else '正式导入'}")
    print(f"时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    
    all_stats = {}
    
    for prefix in SPECIES_CONFIG.keys():
        stats = import_species_type(api_client, prefix, dry_run)
        if stats:
            all_stats[prefix] = stats
    
    # 总结
    print()
    print("=" * 60)
    print("导入总结")
    print("=" * 60)
    
    total_inserted = sum(s['inserted'] for s in all_stats.values())
    total_skipped = sum(s['skipped'] for s in all_stats.values())
    total_errors = sum(len(s['errors']) for s in all_stats.values())
    
    print(f"总导入: {total_inserted}")
    print(f"总跳过: {total_skipped}")
    print(f"总错误: {total_errors}")
    
    return all_stats


# ============================================================
# 主函数
# ============================================================

def main():
    import argparse
    
    parser = argparse.ArgumentParser(description='物种数据导入工具 - 水生态系统')
    parser.add_argument('--dry-run', '-d', action='store_true', 
                        help='预览模式，不实际调用 API')
    parser.add_argument('--type', '-t', choices=['MA', 'AQ', 'PH', 'ZO', 'FI', 'ALL'],
                        default='ALL', help='要导入的物种类型')
    parser.add_argument('--api-url', default=API_CONFIG['base_url'], 
                        help='API 地址')
    parser.add_argument('--username', '-u', default=API_CONFIG['username'], 
                        help='登录用户名')
    parser.add_argument('--password', '-p', default=API_CONFIG['password'], 
                        help='登录密码')
    parser.add_argument('--batch-size', type=int, default=API_CONFIG['batch_size'],
                        help='每批导入数量')
    
    args = parser.parse_args()
    
    # 更新配置
    API_CONFIG['base_url'] = args.api_url
    API_CONFIG['username'] = args.username
    API_CONFIG['password'] = args.password
    API_CONFIG['batch_size'] = args.batch_size
    
    # 创建 API 客户端
    api_client = WaterEcologyAPI(
        base_url=API_CONFIG['base_url'],
        username=API_CONFIG['username'],
        password=API_CONFIG['password']
    )
    
    # 非预览模式需要登录
    if not args.dry_run:
        print(f"连接到: {API_CONFIG['base_url']}")
        if not api_client.login():
            print("无法登录，退出")
            sys.exit(1)
    
    if args.type == 'ALL':
        import_all(api_client, dry_run=args.dry_run)
    else:
        import_species_type(api_client, args.type, dry_run=args.dry_run)


if __name__ == '__main__':
    main()
