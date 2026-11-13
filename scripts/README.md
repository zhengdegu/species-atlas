# 物种数据导入脚本

本目录包含两个独立的导入脚本，分别用于导入数据到不同的系统。

## 脚本说明

### 1. `import_species.py` - Species Atlas 独立后端

将 Excel 数据直接导入到 Species Atlas 的 MySQL 数据库。

**功能特点：**
- 直接数据库连接，速度快
- 支持 upsert（存在则更新，不存在则插入）
- 自动读取 DNA 序列文件
- 自动查找照片文件

**使用方法：**

```bash
# 预览模式（不写入数据库）
python import_species.py --dry-run

# 导入所有类型
python import_species.py

# 只导入特定类型
python import_species.py --type AQ    # 水生植物
python import_species.py --type MA    # 底栖动物
python import_species.py --type PH    # 浮游植物
python import_species.py --type ZO    # 浮游动物
python import_species.py --type FI    # 鱼类

# 指定数据库密码
python import_species.py -p root123456
```

**数据库配置：**
- 主机: localhost
- 端口: 3306
- 用户: root
- 密码: root123456（可通过 -p 参数修改）
- 数据库: species_atlas

---

### 2. `import_water_ecology.py` - Water Ecology 水生态项目

通过 API 接口将 Excel 数据导入到水生态系统。

**功能特点：**
- 通过 REST API 导入，支持远程服务器
- 支持批量导入（每批 50 条）
- 自动登录获取 Token
- 自动读取 DNA 序列文件

**使用方法：**

```bash
# 预览模式（不调用 API）
python import_water_ecology.py --dry-run

# 导入所有类型
python import_water_ecology.py

# 只导入特定类型
python import_water_ecology.py --type AQ    # 水生植物
python import_water_ecology.py --type MA    # 底栖动物
python import_water_ecology.py --type PH    # 浮游植物
python import_water_ecology.py --type ZO    # 浮游动物
python import_water_ecology.py --type FI    # 鱼类

# 指定 API 地址
python import_water_ecology.py --api-url http://localhost:8080

# 指定登录账号
python import_water_ecology.py -u admin -p admin123

# 调整批量大小
python import_water_ecology.py --batch-size 100
```

**API 配置：**
- 默认地址: http://116.131.53.50:9521/prod-api
- 用户名: admin
- 密码: admin123
- 批量大小: 50

---

## 数据源

Excel 文件和序列文件位于 `图谱数据/` 目录：

| 类型 | Excel 文件 | 序列目录 | 照片目录 |
|------|-----------|----------|----------|
| 底栖动物 (MA) | 底栖动物/底栖动物.xls | (暂无) | 底栖动物/常见水生生物图谱-底栖动物 |
| 水生植物 (AQ) | 水生植物/水生植物.xls | 水生植物/水生植物序列 | 水生植物/常见水生生物图谱-水生植物 |
| 浮游植物 (PH) | 浮游植物/浮游植物.xls | 浮游植物/浮游植物序列 | 浮游植物/常见水生生物图谱-浮游植物 |
| 浮游动物 (ZO) | 浮游动物/浮游动物.xls | 浮游动物/浮游动物序列 | 浮游动物/常见水生生物图谱-浮游动物 |
| 鱼类 (FI) | 鱼类/鱼类.xlsx | 鱼类/鱼类序列 | 鱼类/常见水生生物图谱-鱼类 |

---

## 数据量统计

| 类型 | 中文名 | 数据条数 |
|------|--------|----------|
| MA | 底栖动物 | ~304 |
| AQ | 水生植物 | ~212 |
| PH | 浮游植物 | ~257 |
| ZO | 浮游动物 | ~44 |
| FI | 鱼类 | ~237 |
| **总计** | | **~1054** |

---

## 依赖安装

```bash
pip install pandas openpyxl xlrd requests mysql-connector-python
```

---

## 注意事项

1. **预览模式**：首次运行建议使用 `--dry-run` 预览，确认数据无误后再正式导入
2. **数据库/API 需要先启动**：确保目标系统已经运行
3. **编码问题**：Excel 文件和序列文件应为 UTF-8 编码
4. **照片路径**：导入的是相对路径，实际使用时需要配合文件服务器
5. **底栖动物序列**：目前暂无序列文件，后续补充后会自动读取
