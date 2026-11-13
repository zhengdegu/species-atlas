# -*- coding: utf-8 -*-
"""
物种数据导入脚本 - Species Atlas 独立后端
从 Excel 读取物种数据，从序列文件读取 DNA 序列，导入到数据库
支持 upsert（存在则更新，不存在则插入）

用法:
    python import_species.py                    # 导入所有类型
    python import_species.py --type AQ          # 只导入水生植物
    python import_species.py --dry-run          # 预览模式
    python import_species.py -p root123456      # 指定数据库密码
"""

import os
import re
import sys
import json
from pathlib import Path
from datetime import datetime

try:
    import pandas as pd
    import mysql.connector
except ImportError as e:
    print(f"缺少依赖: {e}")
    print("请运行: pip install pandas openpyxl xlrd mysql-connector-python")
    sys.exit(1)

# ============================================================
# 配置
# ============================================================

# 项目根目录
BASE_DIR = Path(__file__).parent.parent

# 数据库配置（生产环境）
DB_CONFIG = {
    'host': 'localhost',
    'port': 3306,
    'user': 'root',
    'password': 'root123456',  # 生产环境密码
    'database': 'species_atlas',
    'charset': 'utf8mb4'
}

# 物种类型配置
SPECIES_CONFIG = {
    'MA': {
        'table': 'benthic_animal',
        'name': '底栖动物',
        'excel': '图谱数据/底栖动物/底栖动物.xls',
        'sequence_dir': None,  # 暂无序列目录
        'photo_dir': '图谱数据/底栖动物/常见水生生物图谱-底栖动物'
    },
    'AQ': {
        'table': 'aquatic_plant',
        'name': '水生植物',
        'excel': '图谱数据/水生植物/水生植物.xls',
        'sequence_dir': '图谱数据/水生植物/水生植物序列',
        'photo_dir': '图谱数据/水生植物/常见水生生物图谱-水生植物'
    },
    'PH': {
        'table': 'phytoplankton',
        'name': '浮游植物',
        'excel': '图谱数据/浮游植物/浮游植物.xls',
        'sequence_dir': '图谱数据/浮游植物/浮游植物序列',
        'photo_dir': '图谱数据/浮游植物/常见水生生物图谱-浮游植物'
    },
    'ZO': {
        'table': 'zooplankton',
        'name': '浮游动物',
        'excel': '图谱数据/浮游动物/浮游动物.xls',
        'sequence_dir': '图谱数据/浮游动物/浮游动物序列',
        'photo_dir': '图谱数据/浮游动物/常见水生生物图谱-浮游动物'
    },
    'FI': {
        'table': 'fish',
        'name': '鱼类',
        'excel': '图谱数据/鱼类/鱼类.xlsx',
        'sequence_dir': '图谱数据/鱼类/鱼类序列',
        'photo_dir': '图谱数据/鱼类/常见水生生物图谱-鱼类'
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
    
    # 如果没有匹配到，尝试其他模式
    # 可能全是中文或全是英文
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


def find_photos(photo_dir, species_code):
    """
    查找物种的所有照片
    返回照片路径列表（相对于上传目录）
    """
    if not photo_dir or not species_code:
        return []
    
    photo_path = BASE_DIR / photo_dir
    if not photo_path.exists():
        return []
    
    species_code = str(species_code).strip()
    photos = []
    
    # 支持的图片格式
    extensions = ['.jpg', '.jpeg', '.png', '.gif']
    
    # 查找匹配的图片文件 (例如 MA0002-01.jpg, MA0002-02.jpg)
    for ext in extensions:
        # 精确匹配: MA0002-01.jpg, MA0002-02.jpg 等
        pattern = f"{species_code}-*.{ext[1:]}"
        for photo_file in photo_path.glob(f"{species_code}-*{ext}"):
            # 生成相对路径用于存储
            relative_path = f"species/{photo_file.name}"
            if relative_path not in photos:
                photos.append(relative_path)
    
    # 按文件名排序
    photos.sort()
    return photos


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


def get_species_prefix(species_code):
    """
    从物种编号获取前缀
    例如: MA0002 -> MA
    """
    if not species_code:
        return None
    match = re.match(r'^([A-Z]+)', str(species_code).upper())
    return match.group(1) if match else None


# ============================================================
# 数据库操作
# ============================================================

def get_db_connection():
    """获取数据库连接"""
    return mysql.connector.connect(**DB_CONFIG)


def check_record_exists(cursor, table_name, species_code):
    """检查记录是否存在"""
    sql = f"SELECT id FROM {table_name} WHERE species_code = %s AND deleted = 0"
    cursor.execute(sql, (species_code,))
    result = cursor.fetchone()
    return result[0] if result else None


def insert_or_update_species(cursor, table_name, data):
    """
    插入或更新物种数据 (Upsert)
    """
    species_code = data['species_code']
    
    # 检查是否存在
    existing_id = check_record_exists(cursor, table_name, species_code)
    
    if existing_id:
        # 更新
        sql = f"""
        UPDATE {table_name} SET
            chinese_name = %s,
            latin_name = %s,
            taxonomic_status = %s,
            photo_code = %s,
            photo_paths = %s,
            gene_name = %s,
            dna_sequence_code = %s,
            dna_coding_property = %s,
            reference = %s,
            geographical_distribution = %s,
            morphological_description = %s,
            update_time = NOW()
        WHERE id = %s
        """
        cursor.execute(sql, (
            data['chinese_name'],
            data['latin_name'],
            data['taxonomic_status'],
            data['photo_code'],
            data['photo_paths'],
            data['gene_name'],
            data['dna_sequence_code'],
            data['dna_coding_property'],
            data['reference'],
            data['geographical_distribution'],
            data['morphological_description'],
            existing_id
        ))
        return 'updated', existing_id
    else:
        # 插入
        sql = f"""
        INSERT INTO {table_name} (
            species_code, chinese_name, latin_name, taxonomic_status,
            photo_code, photo_paths, gene_name, dna_sequence_code,
            dna_coding_property, reference, geographical_distribution,
            morphological_description, create_time, deleted
        ) VALUES (
            %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, NOW(), 0
        )
        """
        cursor.execute(sql, (
            data['species_code'],
            data['chinese_name'],
            data['latin_name'],
            data['taxonomic_status'],
            data['photo_code'],
            data['photo_paths'],
            data['gene_name'],
            data['dna_sequence_code'],
            data['dna_coding_property'],
            data['reference'],
            data['geographical_distribution'],
            data['morphological_description']
        ))
        return 'inserted', cursor.lastrowid


# ============================================================
# 导入函数
# ============================================================

def import_species_type(prefix, dry_run=False):
    """
    导入单个物种类型的数据
    
    Args:
        prefix: 物种前缀 (MA, AQ, PH, ZO, FI)
        dry_run: 是否只预览不实际导入
    
    Returns:
        dict: 导入统计结果
    """
    config = SPECIES_CONFIG.get(prefix)
    if not config:
        print(f"错误: 未知的物种前缀 {prefix}")
        return None
    
    print()
    print("=" * 60)
    print(f"导入 {config['name']} ({prefix})")
    print("=" * 60)
    print(f"Excel: {config['excel']}")
    print(f"序列目录: {config['sequence_dir'] or '(无)'}")
    print(f"图片目录: {config['photo_dir']}")
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
    # 跳过说明行和空行
    df = df[df['物种编号'].notna()]
    df = df[~df['物种编号'].astype(str).str.contains('说明|第一|第二|展示|级别', na=False)]
    df = df[df['物种编号'].astype(str).str.match(r'^[A-Z]{2}\d+$', na=False)]
    
    print(f"有效数据行数: {len(df)}")
    
    if dry_run:
        print("\n[预览模式] 不会实际写入数据库")
    
    # 统计
    stats = {
        'total': len(df),
        'inserted': 0,
        'updated': 0,
        'skipped': 0,
        'errors': []
    }
    
    # 连接数据库
    if not dry_run:
        conn = get_db_connection()
        cursor = conn.cursor()
    
    # 逐行处理
    for idx, row in df.iterrows():
        try:
            species_code = str(row['物种编号']).strip()
            
            # 拆分物种名称
            full_name = get_value(row, '物种名称（中文+拉丁名）')
            chinese_name, latin_name = parse_species_name(full_name)
            
            # 读取 DNA 序列
            dna_code = get_value(row, 'DNA序列编号（可链接到序列信息）')
            dna_sequence = read_sequence_file(config['sequence_dir'], dna_code)
            
            # 查找照片
            photos = find_photos(config['photo_dir'], species_code)
            photo_paths_json = json.dumps(photos) if photos else None
            
            # 准备数据
            data = {
                'species_code': species_code,
                'chinese_name': chinese_name,
                'latin_name': latin_name,
                'taxonomic_status': get_value(row, '分类学地位'),
                'photo_code': get_value(row, '照片编号'),
                'photo_paths': photo_paths_json,
                'gene_name': get_value(row, '基因名称'),
                'dna_sequence_code': dna_code,
                'dna_coding_property': dna_sequence,
                'reference': get_value(row, '数据来源（图书，研究文献，测序，网站）'),
                'geographical_distribution': get_value(row, '地理分布'),
                'morphological_description': get_value(row, '形态学描述'),
            }
            
            if dry_run:
                # 预览模式
                seq_status = "有序列" if dna_sequence else "无序列"
                photo_status = f"{len(photos)}张照片" if photos else "无照片"
                print(f"  {species_code}: {chinese_name} ({latin_name}) [{seq_status}] [{photo_status}]")
                stats['inserted'] += 1
            else:
                # 实际导入
                action, record_id = insert_or_update_species(cursor, config['table'], data)
                
                if action == 'inserted':
                    stats['inserted'] += 1
                    print(f"  [新增] {species_code}: {chinese_name}")
                else:
                    stats['updated'] += 1
                    print(f"  [更新] {species_code}: {chinese_name}")
                    
        except Exception as e:
            stats['errors'].append({
                'row': idx,
                'species_code': species_code if 'species_code' in dir() else 'unknown',
                'error': str(e)
            })
            print(f"  [错误] 行 {idx}: {e}")
    
    # 提交事务
    if not dry_run:
        conn.commit()
        cursor.close()
        conn.close()
    
    # 打印统计
    print()
    print("-" * 40)
    print(f"导入完成:")
    print(f"  总数: {stats['total']}")
    print(f"  新增: {stats['inserted']}")
    print(f"  更新: {stats['updated']}")
    print(f"  跳过: {stats['skipped']}")
    print(f"  错误: {len(stats['errors'])}")
    
    if stats['errors']:
        print("\n错误详情:")
        for err in stats['errors'][:10]:  # 只显示前10个
            print(f"  行 {err['row']}: {err['error']}")
    
    return stats


def import_all(dry_run=False):
    """导入所有物种类型"""
    print("=" * 60)
    print("物种数据导入工具")
    print("=" * 60)
    print(f"项目目录: {BASE_DIR}")
    print(f"数据库: {DB_CONFIG['host']}:{DB_CONFIG['port']}/{DB_CONFIG['database']}")
    print(f"模式: {'预览' if dry_run else '正式导入'}")
    print(f"时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    
    all_stats = {}
    
    for prefix in SPECIES_CONFIG.keys():
        stats = import_species_type(prefix, dry_run)
        if stats:
            all_stats[prefix] = stats
    
    # 总结
    print()
    print("=" * 60)
    print("导入总结")
    print("=" * 60)
    
    total_inserted = sum(s['inserted'] for s in all_stats.values())
    total_updated = sum(s['updated'] for s in all_stats.values())
    total_errors = sum(len(s['errors']) for s in all_stats.values())
    
    print(f"总新增: {total_inserted}")
    print(f"总更新: {total_updated}")
    print(f"总错误: {total_errors}")
    
    return all_stats


# ============================================================
# 主函数
# ============================================================

def main():
    import argparse
    
    parser = argparse.ArgumentParser(description='物种数据导入工具')
    parser.add_argument('--dry-run', '-d', action='store_true', 
                        help='预览模式，不实际写入数据库')
    parser.add_argument('--type', '-t', choices=['MA', 'AQ', 'PH', 'ZO', 'FI', 'ALL'],
                        default='ALL', help='要导入的物种类型')
    parser.add_argument('--host', default='localhost', help='数据库主机')
    parser.add_argument('--port', type=int, default=3306, help='数据库端口')
    parser.add_argument('--password', '-p', default='species123', help='数据库密码')
    
    args = parser.parse_args()
    
    # 更新数据库配置
    DB_CONFIG['host'] = args.host
    DB_CONFIG['port'] = args.port
    DB_CONFIG['password'] = args.password
    
    if args.type == 'ALL':
        import_all(dry_run=args.dry_run)
    else:
        import_species_type(args.type, dry_run=args.dry_run)


if __name__ == '__main__':
    main()
