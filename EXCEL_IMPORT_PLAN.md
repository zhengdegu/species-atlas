# Excel 数据导入实施方案

## 1. 数据概览

### 1.1 Excel 文件清单

| 物种类型 | 文件名 | 数据行数 | 列数 |
|---------|--------|---------|------|
| 底栖动物 | 底栖动物.xls | 306 | 10 |
| 水生植物 | 水生植物.xls | 214 | 9 |
| 浮游植物 | 浮游植物.xls | 257 | 9 |
| 浮游动物 | 浮游动物.xls | 44 | 9 |
| 鱼类 | 鱼类.xlsx | 239 | 9 |

**总计**: 约 1060 条物种数据

### 1.2 Excel 列与数据库字段映射

| Excel 列名 | 数据库字段 | 类型 | 说明 |
|-----------|-----------|------|------|
| 物种编号 | species_code | VARCHAR(100) | 如 MA0002, AQ0002, PH0002, ZO0002, FI0002 |
| 物种名称（中文+拉丁名） | chinese_name + latin_name | VARCHAR(200) | 需拆分，格式：`中文名 拉丁名` |
| 分类学地位 | taxonomic_status | VARCHAR(500) | 门；纲；目；科；属 |
| 照片编号 | photo_code | VARCHAR(100) | 如 MA0002-01 |
| 基因名称 | gene_name | VARCHAR(200) | 如 线粒体基因组、ITS、18S、COⅠ |
| DNA序列编号 | dna_sequence_code | VARCHAR(100) | GenBank 编号如 A312, EF142976 |
| 数据来源 | reference | TEXT | 参考文献来源 |
| 地理分布 | geographical_distribution | TEXT | 地理分布描述 |
| 形态学描述 | morphological_description | TEXT | 形态特征描述 |
| **序列文件内容** | **dna_coding_property** | **TEXT** | **从 `{DNA序列编号}.txt` 文件读取 FASTA 格式序列** |

### 1.3 序列文件说明

序列文件存放在各物种类型的 `序列` 子目录中，以 DNA序列编号 命名：

```
图谱数据/
├── 水生植物/
│   └── 水生植物序列/
│       ├── EF142976.txt    ← DNA序列编号.txt
│       ├── EF142988.txt
│       └── ...
├── 浮游植物/浮游植物序列/
├── 浮游动物/浮游动物序列/
└── 鱼类/鱼类序列/
```

**序列文件格式 (FASTA)**：
```
>EF142976.1 Vallisneria denseserrulata voucher Chen 20 (WBG) internal transcribed spacer 1...
TTTCCGTAGGTGAACCTGCGGAAGGATCATTGTAGGAATGTCTACATATCTGCAAACGTGAACTTGTGAT
CGTCTGGTTGGGGGTGGGGTGCTGCATATGCTGCTCCCTTCCCCCCATCCCTCCGCGAGTCCCGGGTTCC
...
```

**注意**：底栖动物没有序列目录，其 `dna_coding_property` 字段保持为空或使用 Excel 中的备注。

### 1.4 序列文件统计

| 物种类型 | 序列文件数 | 目录路径 |
|---------|----------|---------|
| 底栖动物 | ❌ 无 | - |
| 水生植物 | 202 | 水生植物序列/ |
| 浮游植物 | 249 | 浮游植物序列/ |
| 浮游动物 | 42 | 浮游动物序列/ |
| 鱼类 | 229 | 鱼类序列/ |

### 1.3 物种编号前缀规则

| 类型 | 前缀 | 示例 |
|-----|------|------|
| 底栖动物 (benthic_animal) | MA | MA0001, MA0002 |
| 水生植物 (aquatic_plant) | AQ | AQ0001, AQ0002 |
| 浮游植物 (phytoplankton) | PH | PH0001, PH0002 |
| 浮游动物 (zooplankton) | ZO | ZO0001, ZO0002 |
| 鱼类 (fish) | FI | FI0001, FI0002 |

---

## 2. 技术方案

### 2.1 方案选择

推荐 **方案 A: 后端 API 批量导入**

| 方案 | 优点 | 缺点 |
|-----|------|------|
| **A. 后端 API 导入** | 支持数据校验、事务、日志；可复用 | 需要开发导入接口 |
| B. 数据库直接导入 | 速度快 | 无校验、无审计日志 |
| C. 独立脚本导入 | 简单快速 | 一次性使用、难维护 |

### 2.2 实施架构

```
┌─────────────────┐     ┌──────────────────┐     ┌──────────────────┐
│   Excel 文件    │────▶│  后端导入 API    │────▶│    MySQL 数据库  │
│  (.xls/.xlsx)   │     │  /import/excel   │     │  5 张物种表      │
└─────────────────┘     └──────────────────┘     └──────────────────┘
                               │
                               ▼
                        ┌──────────────────┐
                        │   图片文件目录   │
                        │  /uploads/species│
                        └──────────────────┘
```

### 2.3 后端实现

#### 2.3.1 新增依赖 (pom.xml)

```xml
<!-- Apache POI - Excel 处理 -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi</artifactId>
    <version>5.2.5</version>
</dependency>
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>
```

#### 2.3.2 导入 API 设计

```
POST /api/import/species/{type}
Content-Type: multipart/form-data

参数:
- file: Excel 文件
- type: benthic-animal | aquatic-plant | phytoplankton | fish | zooplankton

响应:
{
  "success": true,
  "message": "导入完成",
  "data": {
    "total": 306,
    "success": 300,
    "failed": 6,
    "errors": [
      {"row": 10, "error": "物种编号重复"},
      ...
    ]
  }
}
```

#### 2.3.3 核心代码结构

```
species-atlas-server/
└── src/main/java/com/species/atlas/
    ├── controller/
    │   └── ImportController.java          # 导入接口
    ├── service/
    │   ├── ImportService.java             # 导入服务接口
    │   └── impl/
    │       └── ImportServiceImpl.java     # 导入服务实现
    ├── dto/
    │   ├── request/
    │   │   └── ExcelImportRequest.java    # 导入请求 DTO
    │   └── response/
    │       └── ImportResult.java          # 导入结果 DTO
    └── util/
        └── ExcelParser.java               # Excel 解析工具
```

---

## 3. 数据处理规则

### 3.1 物种名称拆分

Excel 中的「物种名称（中文+拉丁名）」需要拆分为两个字段：

```
输入: "江西等蜉Isonychia kiangsinensis"
输出:
  - chinese_name: "江西等蜉"
  - latin_name: "Isonychia kiangsinensis"

规则:
1. 中文名: 从开头到第一个英文字母之前
2. 拉丁名: 从第一个英文字母开始到结尾
```

### 3.2 DNA 序列读取 (重要!)

**dna_coding_property 字段** 应从序列文件中读取，而非 Excel：

```
处理流程:
1. 从 Excel 获取 DNA序列编号（如 EF142976）
2. 在对应的序列目录中查找文件: {DNA序列编号}.txt
3. 读取文件的完整内容（FASTA 格式）
4. 存入 dna_coding_property 字段

示例:
  Excel: DNA序列编号 = "EF142976"
  文件: 图谱数据/水生植物/水生植物序列/EF142976.txt
  内容: >EF142976.1 Vallisneria denseserrulata...\nTTTCCGTAGGTGAACCTGCGGAAGGATCATT...
```

**序列目录映射**:
| 物种类型 | 序列目录 |
|---------|---------|
| 水生植物 (AQ) | 图谱数据/水生植物/水生植物序列/ |
| 浮游植物 (PH) | 图谱数据/浮游植物/浮游植物序列/ |
| 浮游动物 (ZO) | 图谱数据/浮游动物/浮游动物序列/ |
| 鱼类 (FI) | 图谱数据/鱼类/鱼类序列/ |
| 底栖动物 (MA) | ❌ 无序列目录 |

### 3.3 图片路径处理

```
输入: 照片编号 "MA0002-01"
输出: photo_paths = ["species/benthic-animal/MA0002/MA0002-01.jpg"]

规则:
1. 根据物种编号前缀确定类型目录
2. 照片存放在 /uploads/species/{type}/{species_code}/ 目录
3. 支持多张照片: MA0002-01, MA0002-02, ...
```

### 3.3 数据清洗规则

| 场景 | 处理方式 |
|-----|---------|
| 空行 | 跳过 |
| 物种编号为空 | 跳过并记录错误 |
| 物种编号重复 | 更新已有记录 (upsert) |
| 特殊字符 | 保留（形态描述中可能有换行符） |
| 前后空格 | 自动 trim |

### 3.4 跳过的行

根据数据分析，Excel 第 2-3 行为说明行，需要跳过：
- 第 1 行: 合并单元格标题（如"列表展示信息"）
- 第 2 行: 列名
- 第 3 行: 说明行（如"第一级别"、"第二级"、"列表页展示用"）
- 第 4 行: 开始为有效数据

---

## 4. 图片导入方案

### 4.1 图片目录结构

```
图谱数据/
├── 底栖动物/
│   └── 常见水生生物图谱-底栖动物/
│       ├── MA0002-01.jpg
│       ├── MA0002-02.jpg
│       └── ...
├── 水生植物/
│   ├── 常见水生生物图谱-水生植物/
│   └── 水生植物序列/
├── 浮游植物/
│   ├── 常见水生生物图谱-浮游植物/
│   └── 浮游植物序列/
├── 浮游动物/
│   ├── 常见水生生物图谱-浮游动物/
│   └── 浮游动物序列/
└── 鱼类/
    ├── 常见水生生物图谱-鱼类/
    └── 鱼类序列/
```

### 4.2 图片处理流程

1. **扫描图片目录**: 根据物种编号查找对应图片
2. **复制到上传目录**: 复制到 `/uploads/species/{type}/{code}/`
3. **生成 JSON 路径**: 存储到 `photo_paths` 字段

---

## 5. 实施步骤

### 阶段一: 后端开发 (预计 2-3 小时)

1. [ ] 添加 Apache POI 依赖
2. [ ] 创建 ExcelParser 工具类
3. [ ] 创建 ImportService 服务
4. [ ] 创建 ImportController 接口
5. [ ] 添加数据校验和错误处理

### 阶段二: 数据导入 (预计 1 小时)

1. [ ] 启动 MySQL 数据库
2. [ ] 启动后端服务
3. [ ] 依次导入 5 类物种 Excel
4. [ ] 验证导入结果

### 阶段三: 图片处理 (预计 1-2 小时)

1. [ ] 扫描并整理图片文件
2. [ ] 复制到上传目录
3. [ ] 更新数据库中的 photo_paths 字段

### 阶段四: 验证和优化 (预计 1 小时)

1. [ ] 验证数据完整性
2. [ ] 测试前端展示
3. [ ] 处理异常数据

---

## 6. API 接口详细设计

### 6.1 Excel 批量导入

```http
POST /api/import/species/{type}
Authorization: Bearer {token}
Content-Type: multipart/form-data

表单字段:
- file: Excel 文件 (必需)
- skipRows: 跳过的行数，默认 3 (可选)
- updateExisting: 是否更新已存在的记录，默认 true (可选)
```

**响应示例:**

```json
{
  "code": 200,
  "message": "导入完成",
  "data": {
    "total": 306,
    "inserted": 290,
    "updated": 10,
    "skipped": 3,
    "failed": 3,
    "errors": [
      {
        "row": 15,
        "speciesCode": "MA0015",
        "field": "chinese_name",
        "error": "物种名称不能为空"
      }
    ]
  }
}
```

### 6.2 导入进度查询 (大文件时)

```http
GET /api/import/progress/{importId}
```

### 6.3 导入模板下载

```http
GET /api/import/template/{type}
```

---

## 7. 前端支持 (可选)

如需前端导入界面，可添加：

1. **导入按钮**: 在物种管理页面添加「导入 Excel」按钮
2. **文件选择器**: 支持拖拽上传
3. **进度显示**: 显示导入进度和结果
4. **错误报告**: 下载失败记录

---

## 8. 备选方案：Python 脚本导入

如果不想修改后端代码，可使用独立 Python 脚本：

```python
# import_species.py
# -*- coding: utf-8 -*-
import pandas as pd
import mysql.connector
import json
import re
import os

# 数据库连接配置
DB_CONFIG = {
    'host': 'localhost',
    'port': 3306,
    'user': 'root',
    'password': 'species123',
    'database': 'species_atlas'
}

# 物种类型配置
SPECIES_CONFIG = {
    'MA': {
        'table': 'benthic_animal',
        'name': '底栖动物',
        'excel': r'图谱数据\底栖动物\底栖动物.xls',
        'sequence_dir': None,  # 无序列目录
        'photo_dir': r'图谱数据\底栖动物'
    },
    'AQ': {
        'table': 'aquatic_plant',
        'name': '水生植物',
        'excel': r'图谱数据\水生植物\水生植物.xls',
        'sequence_dir': r'图谱数据\水生植物\水生植物序列',
        'photo_dir': r'图谱数据\水生植物\常见水生生物图谱-水生植物'
    },
    'PH': {
        'table': 'phytoplankton',
        'name': '浮游植物',
        'excel': r'图谱数据\浮游植物\浮游植物.xls',
        'sequence_dir': r'图谱数据\浮游植物\浮游植物序列',
        'photo_dir': r'图谱数据\浮游植物\常见水生生物图谱-浮游植物'
    },
    'ZO': {
        'table': 'zooplankton',
        'name': '浮游动物',
        'excel': r'图谱数据\浮游动物\浮游动物.xls',
        'sequence_dir': r'图谱数据\浮游动物\浮游动物序列',
        'photo_dir': r'图谱数据\浮游动物\常见水生生物图谱-浮游动物'
    },
    'FI': {
        'table': 'fish',
        'name': '鱼类',
        'excel': r'图谱数据\鱼类\鱼类.xlsx',
        'sequence_dir': r'图谱数据\鱼类\鱼类序列',
        'photo_dir': r'图谱数据\鱼类\常见水生生物图谱-鱼类'
    },
}

def parse_species_name(name):
    """拆分中文名和拉丁名"""
    if pd.isna(name) or not name:
        return '', ''
    match = re.search(r'^([\u4e00-\u9fa5]+)(.+)$', str(name).strip())
    if match:
        return match.group(1).strip(), match.group(2).strip()
    return str(name).strip(), ''

def read_sequence_file(sequence_dir, dna_code):
    """读取 DNA 序列文件内容"""
    if not sequence_dir or not dna_code or pd.isna(dna_code):
        return None
    
    # 尝试多种文件名格式
    possible_names = [
        f'{dna_code}.txt',
        f'{dna_code.replace("-", "_")}.txt',
        f'{dna_code.replace("_", "-")}.txt',
    ]
    
    for filename in possible_names:
        filepath = os.path.join(sequence_dir, filename)
        if os.path.exists(filepath):
            with open(filepath, 'r', encoding='utf-8') as f:
                return f.read().strip()
    
    return None

def get_species_prefix(species_code):
    """从物种编号获取前缀"""
    if not species_code:
        return None
    match = re.match(r'^([A-Z]+)', str(species_code))
    return match.group(1) if match else None

def import_species(prefix):
    """导入单个物种类型的数据"""
    config = SPECIES_CONFIG.get(prefix)
    if not config:
        print(f'未知的物种前缀: {prefix}')
        return
    
    print(f'\n=== 导入 {config["name"]} ===')
    print(f'Excel: {config["excel"]}')
    print(f'序列目录: {config["sequence_dir"]}')
    
    # 读取 Excel，跳过前两行（合并标题 + 说明行）
    df = pd.read_excel(config['excel'], header=1)
    # 跳过说明行（第一行数据通常是说明）
    df = df[df['物种编号'].notna()]
    df = df[~df['物种编号'].astype(str).str.contains('说明|第一|第二|展示')]
    
    print(f'有效数据行数: {len(df)}')
    
    conn = mysql.connector.connect(**DB_CONFIG)
    cursor = conn.cursor()
    
    success_count = 0
    error_count = 0
    errors = []
    
    for idx, row in df.iterrows():
        try:
            species_code = str(row['物种编号']).strip()
            if not species_code or species_code == 'nan':
                continue
            
            # 拆分物种名称
            chinese_name, latin_name = parse_species_name(
                row.get('物种名称（中文+拉丁名）', '')
            )
            
            # 读取 DNA 序列
            dna_code = str(row.get('DNA序列编号（可链接到序列信息）', '')).strip()
            dna_sequence = read_sequence_file(config['sequence_dir'], dna_code)
            
            sql = f'''
            INSERT INTO {config['table']} 
            (species_code, chinese_name, latin_name, taxonomic_status, 
             photo_code, gene_name, dna_sequence_code, dna_coding_property,
             reference, geographical_distribution, morphological_description)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
            ON DUPLICATE KEY UPDATE
            chinese_name = VALUES(chinese_name),
            latin_name = VALUES(latin_name),
            taxonomic_status = VALUES(taxonomic_status),
            photo_code = VALUES(photo_code),
            gene_name = VALUES(gene_name),
            dna_sequence_code = VALUES(dna_sequence_code),
            dna_coding_property = VALUES(dna_coding_property),
            reference = VALUES(reference),
            geographical_distribution = VALUES(geographical_distribution),
            morphological_description = VALUES(morphological_description)
            '''
            
            def get_value(col):
                val = row.get(col, '')
                return str(val).strip() if pd.notna(val) else None
            
            cursor.execute(sql, (
                species_code,
                chinese_name,
                latin_name,
                get_value('分类学地位'),
                get_value('照片编号'),
                get_value('基因名称'),
                dna_code if dna_code != 'nan' else None,
                dna_sequence,
                get_value('数据来源（图书，研究文献，测序，网站）'),
                get_value('地理分布'),
                get_value('形态学描述')
            ))
            success_count += 1
            
        except Exception as e:
            error_count += 1
            errors.append({'row': idx, 'error': str(e)})
    
    conn.commit()
    cursor.close()
    conn.close()
    
    print(f'导入完成: 成功 {success_count}, 失败 {error_count}')
    if errors:
        print('错误列表:')
        for e in errors[:5]:  # 只显示前5个错误
            print(f'  行 {e["row"]}: {e["error"]}')

def main():
    """主函数"""
    print('物种数据导入工具')
    print('=' * 50)
    
    # 导入所有物种类型
    for prefix in SPECIES_CONFIG.keys():
        import_species(prefix)
    
    print('\n全部导入完成!')

if __name__ == '__main__':
    main()
```

**运行方式:**
```bash
cd species-atlas
pip install pandas openpyxl xlrd mysql-connector-python
python import_species.py
```

---

## 9. 推荐实施顺序

1. **优先选择**: 方案 A (后端 API 导入)
   - 更规范、可复用
   - 支持权限控制和审计
   - 便于后续维护

2. **快速验证**: 方案 C (Python 脚本)
   - 适合一次性导入
   - 快速验证数据

---

## 10. 注意事项

1. **备份数据**: 导入前先备份数据库
2. **测试环境**: 先在开发环境测试
3. **数据校验**: 导入后检查数据完整性
4. **图片关联**: 确保图片路径正确
5. **编码问题**: 注意 Excel 文件编码 (UTF-8)

---

## 附录: 数据库表结构

所有 5 张物种表结构相同：

| 字段 | 类型 | 说明 |
|-----|------|------|
| id | BIGINT | 主键 |
| serial_number | VARCHAR(50) | 序号 |
| species_code | VARCHAR(100) | 物种编码 |
| chinese_name | VARCHAR(200) | 中文名 |
| latin_name | VARCHAR(200) | 拉丁名 |
| photo_code | VARCHAR(100) | 照片编码 |
| photo_paths | JSON | 照片路径列表 |
| taxonomic_status | VARCHAR(500) | 分类地位 |
| gene_name | VARCHAR(200) | 基因名称 |
| dna_sequence_code | VARCHAR(100) | DNA序列编码 |
| dna_coding_property | TEXT | DNA编码属性 |
| morphological_description | TEXT | 形态描述 |
| reference | TEXT | 参考文献 |
| geographical_distribution | TEXT | 地理分布 |
| create_by | BIGINT | 创建人 |
| create_time | DATETIME | 创建时间 |
| update_by | BIGINT | 更新人 |
| update_time | DATETIME | 更新时间 |
| deleted | TINYINT | 删除标记 |
