# -*- coding: utf-8 -*-
"""
图片格式转换脚本
将 TIF 和 WebP 格式转换为 JPG，以便在浏览器中显示
"""

import os
import sys
from pathlib import Path

try:
    from PIL import Image
except ImportError:
    print("请先安装 Pillow: pip install Pillow")
    sys.exit(1)

# 配置
BASE_DIR = Path(__file__).parent.parent / "图谱数据"
CONVERT_WEBP = True  # 是否转换 WebP（设为 False 则保留 WebP）
JPG_QUALITY = 90     # JPG 压缩质量 (1-100)
DELETE_ORIGINAL = False  # 转换后是否删除原文件（建议先设为 False 测试）


def convert_to_jpg(source_path: Path, delete_original: bool = False) -> bool:
    """
    将图片转换为 JPG 格式
    
    Args:
        source_path: 源文件路径
        delete_original: 是否删除原文件
    
    Returns:
        转换是否成功
    """
    try:
        # 目标文件路径
        target_path = source_path.with_suffix('.jpg')
        
        # 如果目标文件已存在，跳过
        if target_path.exists():
            print(f"  跳过 (已存在): {target_path.name}")
            return True
        
        # 打开图片
        with Image.open(source_path) as img:
            # 如果是 RGBA 模式（有透明通道），转换为 RGB
            if img.mode in ('RGBA', 'LA', 'P'):
                # 创建白色背景
                background = Image.new('RGB', img.size, (255, 255, 255))
                if img.mode == 'P':
                    img = img.convert('RGBA')
                background.paste(img, mask=img.split()[-1] if img.mode == 'RGBA' else None)
                img = background
            elif img.mode != 'RGB':
                img = img.convert('RGB')
            
            # 保存为 JPG
            img.save(target_path, 'JPEG', quality=JPG_QUALITY, optimize=True)
        
        # 获取文件大小
        original_size = source_path.stat().st_size / 1024  # KB
        new_size = target_path.stat().st_size / 1024  # KB
        
        print(f"  [OK] 转换成功: {source_path.name} -> {target_path.name}")
        print(f"    大小: {original_size:.1f} KB -> {new_size:.1f} KB ({(1 - new_size/original_size)*100:.1f}% 压缩)")
        
        # 删除原文件
        if delete_original:
            source_path.unlink()
            print(f"    已删除原文件")
        
        return True
        
    except Exception as e:
        print(f"  [FAIL] 转换失败: {source_path.name} - {e}")
        return False


def find_images(base_dir: Path, extensions: list) -> list:
    """
    递归查找指定扩展名的图片
    """
    images = []
    for ext in extensions:
        images.extend(base_dir.rglob(f"*{ext}"))
    return images


def main():
    print("=" * 60)
    print("图片格式转换工具")
    print("=" * 60)
    print(f"基础目录: {BASE_DIR}")
    print(f"JPG 质量: {JPG_QUALITY}")
    print(f"转换 WebP: {'是' if CONVERT_WEBP else '否'}")
    print(f"删除原文件: {'是' if DELETE_ORIGINAL else '否'}")
    print()
    
    if not BASE_DIR.exists():
        print(f"错误: 目录不存在 - {BASE_DIR}")
        return
    
    # 查找 TIF 文件
    print("=" * 60)
    print("1. 转换 TIF 文件")
    print("=" * 60)
    tif_files = find_images(BASE_DIR, ['.tif', '.tiff'])
    print(f"找到 {len(tif_files)} 个 TIF 文件")
    
    tif_success = 0
    for f in tif_files:
        if convert_to_jpg(f, DELETE_ORIGINAL):
            tif_success += 1
    
    print(f"\nTIF 转换完成: {tif_success}/{len(tif_files)} 成功")
    
    # 查找 WebP 文件
    if CONVERT_WEBP:
        print()
        print("=" * 60)
        print("2. 转换 WebP 文件")
        print("=" * 60)
        webp_files = find_images(BASE_DIR, ['.webp'])
        print(f"找到 {len(webp_files)} 个 WebP 文件")
        
        webp_success = 0
        for f in webp_files:
            if convert_to_jpg(f, DELETE_ORIGINAL):
                webp_success += 1
        
        print(f"\nWebP 转换完成: {webp_success}/{len(webp_files)} 成功")
    
    # 总结
    print()
    print("=" * 60)
    print("转换完成!")
    print("=" * 60)
    
    if not DELETE_ORIGINAL:
        print("\n提示: 原文件已保留。确认转换无误后，可以手动删除原文件，")
        print("或将脚本中的 DELETE_ORIGINAL 设为 True 重新运行。")


if __name__ == '__main__':
    main()
