#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
物种图片批量上传脚本
将本地图片上传到后端存储，并更新数据库中的图片路径
"""

import os
import sys
import json
import requests
import argparse
from pathlib import Path
from concurrent.futures import ThreadPoolExecutor, as_completed

# 配置
API_BASE_URL = "http://localhost:8080/api"
IMAGE_BASE_DIR = Path(r"d:\env\project\species-atlas\图谱数据")

# 物种类型映射
SPECIES_TYPES = {
    "水生植物": {
        "image_dir": "常见水生生物图谱-水生植物",
        "table": "aquatic_plant",
        "prefix": "AQ"
    },
    "底栖动物": {
        "image_dir": "常见水生生物图谱-底栖动物",
        "table": "benthic_animal",
        "prefix": "MA"
    },
    "浮游植物": {
        "image_dir": "常见水生生物图谱-浮游植物",
        "table": "phytoplankton",
        "prefix": "PH"
    },
    "浮游动物": {
        "image_dir": "常见水生生物图谱-浮游动物",
        "table": "zooplankton",
        "prefix": "ZO"
    },
    "鱼类": {
        "image_dir": "常见水生生物图谱-鱼类",
        "table": "fish",
        "prefix": "FI"
    }
}


def login(username: str, password: str) -> str:
    """登录获取 token"""
    url = f"{API_BASE_URL}/auth/login"
    response = requests.post(url, json={"username": username, "password": password})
    data = response.json()
    if data.get("code") == 200:
        return data["data"]["accessToken"]
    else:
        raise Exception(f"登录失败: {data.get('msg')}")


def upload_image(token: str, file_path: Path, species_code: str) -> dict:
    """上传单个图片"""
    url = f"{API_BASE_URL}/file/upload/species"
    headers = {"Authorization": f"Bearer {token}"}
    
    with open(file_path, "rb") as f:
        files = {"file": (file_path.name, f, "image/png")}
        data = {"speciesCode": species_code}
        response = requests.post(url, headers=headers, files=files, data=data)
    
    result = response.json()
    if result.get("code") == 200:
        return result["data"]
    else:
        raise Exception(f"上传失败: {result.get('msg')}")


def get_species_images(species_type: str) -> dict:
    """获取某类型的所有图片文件，按物种编码分组"""
    config = SPECIES_TYPES.get(species_type)
    if not config:
        return {}
    
    image_dir = IMAGE_BASE_DIR / species_type / config["image_dir"]
    if not image_dir.exists():
        print(f"目录不存在: {image_dir}")
        return {}
    
    # 按物种编码分组
    images_by_code = {}
    for file in image_dir.iterdir():
        if file.is_file() and file.suffix.lower() in ['.jpg', '.jpeg', '.png', '.gif', '.webp', '.tif']:
            # 提取物种编码，格式如 AQ0001-01.png -> AQ0001
            name = file.stem
            parts = name.rsplit('-', 1)
            if len(parts) == 2:
                species_code = parts[0]
                if species_code not in images_by_code:
                    images_by_code[species_code] = []
                images_by_code[species_code].append(file)
    
    return images_by_code


def upload_species_type(token: str, species_type: str, dry_run: bool = False) -> tuple:
    """上传某类型的所有图片"""
    config = SPECIES_TYPES.get(species_type)
    if not config:
        print(f"未知物种类型: {species_type}")
        return 0, 0
    
    images_by_code = get_species_images(species_type)
    total_files = sum(len(files) for files in images_by_code.values())
    
    print(f"\n{'='*50}")
    print(f"处理 {species_type}")
    print(f"物种数: {len(images_by_code)}, 图片数: {total_files}")
    print(f"{'='*50}")
    
    if dry_run:
        for code, files in list(images_by_code.items())[:3]:
            print(f"  {code}: {[f.name for f in files]}")
        if len(images_by_code) > 3:
            print(f"  ... 还有 {len(images_by_code) - 3} 个物种")
        return len(images_by_code), total_files
    
    uploaded = 0
    failed = 0
    results = {}  # species_code -> [uploaded_paths]
    
    for species_code, files in images_by_code.items():
        paths = []
        for file in sorted(files):
            try:
                result = upload_image(token, file, species_code)
                paths.append(result["path"])
                uploaded += 1
                print(f"  ✓ {file.name} -> {result['path']}")
            except Exception as e:
                failed += 1
                print(f"  ✗ {file.name}: {e}")
        
        if paths:
            results[species_code] = paths
    
    print(f"\n{species_type} 完成: 上传 {uploaded} 张, 失败 {failed} 张")
    return uploaded, failed


def main():
    parser = argparse.ArgumentParser(description="批量上传物种图片")
    parser.add_argument("-u", "--username", default="admin", help="用户名")
    parser.add_argument("-p", "--password", default="admin123", help="密码")
    parser.add_argument("-t", "--type", help="物种类型（可选，不指定则处理所有）")
    parser.add_argument("--dry-run", action="store_true", help="仅显示将要处理的文件，不实际上传")
    parser.add_argument("--api", default=API_BASE_URL, help="API 地址")
    args = parser.parse_args()
    
    global API_BASE_URL
    API_BASE_URL = args.api
    
    print("="*60)
    print("物种图片批量上传")
    print("="*60)
    print(f"API 地址: {API_BASE_URL}")
    print(f"图片目录: {IMAGE_BASE_DIR}")
    print(f"模式: {'预览' if args.dry_run else '上传'}")
    
    # 登录
    if not args.dry_run:
        print(f"\n登录中...")
        try:
            token = login(args.username, args.password)
            print("登录成功")
        except Exception as e:
            print(f"登录失败: {e}")
            sys.exit(1)
    else:
        token = None
    
    # 处理图片
    types_to_process = [args.type] if args.type else SPECIES_TYPES.keys()
    
    total_uploaded = 0
    total_failed = 0
    
    for species_type in types_to_process:
        if species_type not in SPECIES_TYPES:
            print(f"未知类型: {species_type}")
            continue
        
        uploaded, failed = upload_species_type(token, species_type, args.dry_run)
        total_uploaded += uploaded
        total_failed += failed
    
    print("\n" + "="*60)
    print("汇总")
    print("="*60)
    print(f"总上传: {total_uploaded} 张")
    print(f"总失败: {total_failed} 张")


if __name__ == "__main__":
    main()
