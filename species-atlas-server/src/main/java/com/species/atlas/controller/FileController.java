package com.species.atlas.controller;

import com.species.atlas.dto.response.ApiResponse;
import com.species.atlas.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "文件管理", description = "图片上传、下载接口")
public class FileController {

    private final FileStorageService fileStorageService;

    /**
     * 上传单个物种图片
     */
    @PostMapping("/upload/species")
    @Operation(summary = "上传物种图片", description = "上传单个物种图片，返回存储路径")
    public ApiResponse<Map<String, String>> uploadSpeciesImage(
            @Parameter(description = "图片文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "物种编码，如 AQ0001") @RequestParam("speciesCode") String speciesCode) {
        
        String path = fileStorageService.uploadSpeciesImage(file, speciesCode);
        
        Map<String, String> result = new HashMap<>();
        result.put("path", path);
        result.put("url", "/uploads/" + path);
        
        return ApiResponse.success("上传成功", result);
    }

    /**
     * 批量上传物种图片
     */
    @PostMapping("/upload/species/batch")
    @Operation(summary = "批量上传物种图片", description = "批量上传物种图片，返回存储路径列表")
    public ApiResponse<Map<String, Object>> uploadSpeciesImages(
            @Parameter(description = "图片文件数组") @RequestParam("files") MultipartFile[] files,
            @Parameter(description = "物种编码，如 AQ0001") @RequestParam("speciesCode") String speciesCode) {
        
        List<String> paths = fileStorageService.uploadSpeciesImages(files, speciesCode);
        
        Map<String, Object> result = new HashMap<>();
        result.put("paths", paths);
        result.put("urls", paths.stream().map(p -> "/uploads/" + p).toList());
        result.put("count", paths.size());
        
        return ApiResponse.success("上传成功", result);
    }

    /**
     * 通用文件上传
     */
    @PostMapping("/upload")
    @Operation(summary = "通用文件上传", description = "上传文件到指定目录")
    public ApiResponse<Map<String, String>> uploadFile(
            @Parameter(description = "文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "子目录，默认 species") @RequestParam(value = "dir", defaultValue = "species") String dir,
            @Parameter(description = "文件名前缀（可选）") @RequestParam(value = "prefix", required = false) String prefix) {
        
        String path = fileStorageService.uploadFile(file, dir, prefix);
        
        Map<String, String> result = new HashMap<>();
        result.put("path", path);
        result.put("url", "/uploads/" + path);
        
        return ApiResponse.success("上传成功", result);
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除文件", description = "根据路径删除文件")
    public ApiResponse<Void> deleteFile(
            @Parameter(description = "文件路径，如 species/AQ0001-01.png") @RequestParam("path") String path) {
        
        boolean deleted = fileStorageService.deleteFile(path);
        
        if (deleted) {
            return ApiResponse.success("删除成功", null);
        } else {
            return ApiResponse.error(404, "文件不存在或删除失败");
        }
    }

    /**
     * 检查文件是否存在
     */
    @GetMapping("/exists")
    @Operation(summary = "检查文件是否存在", description = "根据路径检查文件是否存在")
    public ApiResponse<Map<String, Object>> checkFileExists(
            @Parameter(description = "文件路径，如 species/AQ0001-01.png") @RequestParam("path") String path) {
        
        boolean exists = fileStorageService.fileExists(path);
        
        Map<String, Object> result = new HashMap<>();
        result.put("path", path);
        result.put("exists", exists);
        
        return ApiResponse.success(result);
    }

    /**
     * 获取文件（用于查看/下载）
     * 注意：实际生产中，静态文件应该由 Nginx 直接处理，这里仅作为备用
     */
    @GetMapping("/download/{subDir}/{filename}")
    @Operation(summary = "下载文件", description = "根据路径下载文件")
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "子目录") @PathVariable String subDir,
            @Parameter(description = "文件名") @PathVariable String filename) {
        
        String relativePath = subDir + "/" + filename;
        Path filePath = fileStorageService.getFilePath(relativePath);
        
        try {
            Resource resource = new UrlResource(filePath.toUri());
            
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }
            
            // 确定 Content-Type
            String contentType;
            try {
                contentType = Files.probeContentType(filePath);
            } catch (IOException e) {
                contentType = "application/octet-stream";
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
                    
        } catch (MalformedURLException e) {
            log.error("文件路径错误: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
