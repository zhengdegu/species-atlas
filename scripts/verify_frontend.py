# -*- coding: utf-8 -*-
"""
前端自动化验证脚本
验证图谱数据加载、图片显示等功能
使用 Selenium 进行浏览器自动化测试
"""

import os
import sys
import time
import json
import requests
from pathlib import Path
from datetime import datetime

try:
    from selenium import webdriver
    from selenium.webdriver.common.by import By
    from selenium.webdriver.support.ui import WebDriverWait
    from selenium.webdriver.support import expected_conditions as EC
    from selenium.webdriver.chrome.options import Options
    from selenium.webdriver.chrome.service import Service
except ImportError:
    print("缺少 selenium，请运行: pip install selenium webdriver-manager")
    sys.exit(1)

try:
    from webdriver_manager.chrome import ChromeDriverManager
except ImportError:
    print("缺少 webdriver-manager，请运行: pip install webdriver-manager")
    sys.exit(1)

# 配置
BASE_URL = "http://localhost:3000"
API_URL = "http://localhost:8080/api"
SCREENSHOT_DIR = Path(__file__).parent.parent / "screenshots"
SCREENSHOT_DIR.mkdir(exist_ok=True)

# 测试结果
results = {
    "timestamp": datetime.now().isoformat(),
    "tests": [],
    "broken_images": [],
    "summary": {}
}

def setup_driver():
    """设置 Chrome WebDriver"""
    options = Options()
    options.add_argument("--headless")  # 无头模式
    options.add_argument("--window-size=1920,1080")
    options.add_argument("--disable-gpu")
    options.add_argument("--no-sandbox")
    options.add_argument("--disable-dev-shm-usage")
    
    try:
        service = Service(ChromeDriverManager().install())
        driver = webdriver.Chrome(service=service, options=options)
        return driver
    except Exception as e:
        print(f"启动 Chrome 失败: {e}")
        return None

def take_screenshot(driver, name):
    """截图保存"""
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    filename = SCREENSHOT_DIR / f"{name}_{timestamp}.png"
    driver.save_screenshot(str(filename))
    print(f"  截图保存: {filename}")
    return str(filename)

def check_broken_images(driver):
    """检查页面上所有图片是否加载成功"""
    broken = []
    images = driver.find_elements(By.TAG_NAME, "img")
    
    for img in images:
        src = img.get_attribute("src")
        if not src:
            continue
            
        # 检查图片是否加载成功
        is_broken = driver.execute_script("""
            return arguments[0].complete === false || 
                   arguments[0].naturalWidth === 0 ||
                   arguments[0].naturalHeight === 0;
        """, img)
        
        if is_broken:
            broken.append({
                "src": src,
                "alt": img.get_attribute("alt") or "",
                "class": img.get_attribute("class") or ""
            })
    
    return broken

def verify_api_data():
    """验证 API 数据"""
    print("\n========== 验证 API 数据 ==========")
    
    # 登录获取 Token
    try:
        login_response = requests.post(
            f"{API_URL}/auth/login",
            json={"username": "admin", "password": "admin123"},
            timeout=10
        )
        login_data = login_response.json()
        token = login_data.get("data", {}).get("accessToken")
        
        if not token:
            print("  登录失败")
            return False
        
        headers = {"Authorization": f"Bearer {token}"}
        
        # 检查各类物种数据
        species_types = [
            ("底栖动物", "benthic-animal"),
            ("水生植物", "aquatic-plant"),
            ("浮游植物", "phytoplankton"),
            ("浮游动物", "zooplankton"),
            ("鱼类", "fish"),
        ]
        
        total = 0
        for name, endpoint in species_types:
            response = requests.get(
                f"{API_URL}/species/{endpoint}/list?pageNum=1&pageSize=1",
                headers=headers,
                timeout=10
            )
            data = response.json()
            count = data.get("data", {}).get("total", 0)
            total += count
            print(f"  {name}: {count} 条")
            
            results["tests"].append({
                "name": f"API - {name}",
                "status": "PASS" if count > 0 else "FAIL",
                "count": count
            })
        
        results["summary"]["api_total"] = total
        print(f"  总计: {total} 条")
        return True
        
    except Exception as e:
        print(f"  API 验证失败: {e}")
        return False

def verify_frontend_pages(driver):
    """验证前端页面"""
    print("\n========== 验证前端页面 ==========")
    
    # 访问首页
    print("\n1. 首页")
    driver.get(BASE_URL)
    time.sleep(3)
    screenshot = take_screenshot(driver, "01_homepage")
    
    # 检查页面标题
    title = driver.title
    print(f"  页面标题: {title}")
    
    # 检查是否需要登录
    current_url = driver.current_url
    print(f"  当前 URL: {current_url}")
    
    # 检查页面是否有图谱相关内容
    page_source = driver.page_source
    has_species_content = "物种" in page_source or "图谱" in page_source or "species" in page_source.lower()
    print(f"  包含图谱内容: {has_species_content}")
    
    results["tests"].append({
        "name": "首页加载",
        "status": "PASS" if "species" in current_url.lower() or has_species_content else "WARN",
        "url": current_url,
        "screenshot": screenshot
    })
    
    # 检查图片加载
    print("\n2. 检查图片加载")
    broken_images = check_broken_images(driver)
    if broken_images:
        print(f"  发现 {len(broken_images)} 张图片加载失败:")
        for img in broken_images[:10]:
            print(f"    - {img['src'][:80]}...")
        results["broken_images"].extend(broken_images)
        
        # 截图显示问题
        take_screenshot(driver, "02_broken_images")
    else:
        print("  所有图片加载正常")
    
    results["tests"].append({
        "name": "图片加载检查",
        "status": "FAIL" if broken_images else "PASS",
        "broken_count": len(broken_images)
    })
    
    return len(broken_images) == 0

def verify_species_list(driver):
    """验证物种列表页面"""
    print("\n========== 验证物种列表 ==========")
    
    # 尝试不同的路径
    test_paths = [
        "/species",
        "/aquatic-plant",
        "/fish",
        "/#/species",
        "/#/aquatic-plant",
    ]
    
    for path in test_paths:
        try:
            url = BASE_URL + path
            print(f"\n尝试访问: {url}")
            driver.get(url)
            time.sleep(2)
            
            # 检查是否有数据
            page_source = driver.page_source
            
            # 查找可能的数据元素
            cards = driver.find_elements(By.CSS_SELECTOR, ".species-card, .card, [class*='species'], [class*='item']")
            
            if cards:
                print(f"  找到 {len(cards)} 个数据项")
                screenshot = take_screenshot(driver, f"species_list_{path.replace('/', '_')}")
                
                results["tests"].append({
                    "name": f"物种列表 - {path}",
                    "status": "PASS",
                    "count": len(cards),
                    "screenshot": screenshot
                })
                
                # 检查图片
                broken = check_broken_images(driver)
                if broken:
                    print(f"  {len(broken)} 张图片加载失败")
                    results["broken_images"].extend(broken)
                    take_screenshot(driver, f"broken_images_{path.replace('/', '_')}")
                
                return True
                
        except Exception as e:
            print(f"  访问失败: {e}")
    
    print("  未找到物种列表数据")
    return False

def generate_report():
    """生成验证报告"""
    print("\n========== 验证报告 ==========")
    
    # 统计
    total_tests = len(results["tests"])
    passed = sum(1 for t in results["tests"] if t["status"] == "PASS")
    failed = sum(1 for t in results["tests"] if t["status"] == "FAIL")
    warned = sum(1 for t in results["tests"] if t["status"] == "WARN")
    
    results["summary"]["total_tests"] = total_tests
    results["summary"]["passed"] = passed
    results["summary"]["failed"] = failed
    results["summary"]["warned"] = warned
    results["summary"]["broken_images_count"] = len(results["broken_images"])
    
    print(f"总测试数: {total_tests}")
    print(f"通过: {passed}")
    print(f"失败: {failed}")
    print(f"警告: {warned}")
    print(f"加载失败的图片: {len(results['broken_images'])}")
    
    # 保存报告
    report_file = SCREENSHOT_DIR / f"report_{datetime.now().strftime('%Y%m%d_%H%M%S')}.json"
    with open(report_file, 'w', encoding='utf-8') as f:
        json.dump(results, f, ensure_ascii=False, indent=2)
    print(f"\n报告保存: {report_file}")
    
    # 列出所有截图
    print(f"\n截图保存目录: {SCREENSHOT_DIR}")
    for f in SCREENSHOT_DIR.glob("*.png"):
        print(f"  - {f.name}")
    
    return results

def main():
    print("=" * 60)
    print("Species Atlas 前端验证脚本")
    print("=" * 60)
    print(f"前端地址: {BASE_URL}")
    print(f"API 地址: {API_URL}")
    print(f"截图目录: {SCREENSHOT_DIR}")
    print(f"时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    
    # 验证 API 数据
    verify_api_data()
    
    # 设置浏览器
    print("\n初始化浏览器...")
    driver = setup_driver()
    
    if driver:
        try:
            # 验证前端页面
            verify_frontend_pages(driver)
            
            # 验证物种列表
            verify_species_list(driver)
            
        finally:
            driver.quit()
    else:
        print("无法启动浏览器，跳过前端测试")
    
    # 生成报告
    generate_report()
    
    return 0 if results["summary"].get("failed", 0) == 0 else 1

if __name__ == "__main__":
    sys.exit(main())
