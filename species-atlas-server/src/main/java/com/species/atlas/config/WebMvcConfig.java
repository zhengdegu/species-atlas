package com.species.atlas.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Web MVC 配置
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取绝对路径，确保路径格式正确
        File uploadDir = new File(uploadPath);
        String absolutePath = uploadDir.getAbsolutePath();
        
        // 确保路径以 / 结尾（Spring 资源定位器需要）
        if (!absolutePath.endsWith(File.separator)) {
            absolutePath = absolutePath + File.separator;
        }
        
        String resourceLocation = "file:" + absolutePath;
        log.info("配置静态资源映射: /uploads/** -> {}", resourceLocation);
        
        // 配置静态资源映射，添加缓存控制（浏览器缓存7天）
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation)
                .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic());
    }
}
