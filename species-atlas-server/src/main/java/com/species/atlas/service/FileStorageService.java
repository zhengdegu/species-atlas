package com.species.atlas.service;

import com.species.atlas.exception.BusinessException;
import com.species.atlas.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件存储服务
 * 负责图片的上传、存储和删除
 */
@Service
@Slf4j
public class FileStorageService {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Value("${file.upload.allowed-types:jpg,jpeg,png,gif,webp}")
    private String allowedTypes;

    @Value("${file.upload.max-size:10485760}")
    private long maxFileSize;

    private Path rootLocation;
    private List<String> allowedExtensions;

    @PostConstruct
    public void init() {
        this.rootLocation = Paths.get(uploadPath).toAbsolutePath().normalize();
        this.allowedExtensions = Arrays.asList(allowedTypes.toLowerCase().split(","));
        
        try {
            Files.createDirectories(rootLocation);
            // 创建 species 子目录
            Files.createDirectories(rootLocation.resolve("species"));
            log.info("文件存储目录初始化完成: {}", rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("无法创建上传目录", e);
        }
    }

    /**
     * 上传物种图片
     * @param file 上传的文件
     * @param speciesCode 物种编码（用于生成文件名）
     * @return 存储路径（相对路径，如 species/AQ0001-01.png）
     */
    public String uploadSpeciesImage(MultipartFile file, String speciesCode) {
        return uploadFile(file, "species", speciesCode);
    }

    /**
     * 上传文件
     * @param file 上传的文件
     * @param subDir 子目录
     * @param prefix 文件名前缀
     * @return 存储路径（相对路径）
     */
    public String uploadFile(MultipartFile file, String subDir, String prefix) {
        // 验证文件
        validateFile(file);

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getFileExtension(originalFilename);
        
        // 生成唯一文件名
        String newFilename = generateFilename(prefix, extension);
        
        // 确定存储路径
        Path targetDir = rootLocation.resolve(subDir);
        Path targetPath = targetDir.resolve(newFilename);

        try {
            // 确保目录存在
            Files.createDirectories(targetDir);
            
            // 保存文件
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
            
            log.info("文件上传成功: {}", targetPath);
            
            // 返回相对路径
            return subDir + "/" + newFilename;
            
        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量上传物种图片
     * @param files 文件数组
     * @param speciesCode 物种编码
     * @return 存储路径列表
     */
    public List<String> uploadSpeciesImages(MultipartFile[] files, String speciesCode) {
        return Arrays.stream(files)
                .map(file -> uploadSpeciesImage(file, speciesCode))
                .toList();
    }

    /**
     * 删除文件
     * @param relativePath 相对路径（如 species/AQ0001-01.png）
     * @return 是否删除成功
     */
    public boolean deleteFile(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            return false;
        }
        
        try {
            Path filePath = rootLocation.resolve(relativePath).normalize();
            
            // 安全检查：确保路径在上传目录内
            if (!filePath.startsWith(rootLocation)) {
                log.warn("非法的文件路径: {}", relativePath);
                return false;
            }
            
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("文件删除成功: {}", relativePath);
            }
            return deleted;
            
        } catch (IOException e) {
            log.error("文件删除失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查文件是否存在
     * @param relativePath 相对路径
     * @return 是否存在
     */
    public boolean fileExists(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            return false;
        }
        Path filePath = rootLocation.resolve(relativePath).normalize();
        return Files.exists(filePath) && filePath.startsWith(rootLocation);
    }

    /**
     * 获取文件的绝对路径
     * @param relativePath 相对路径
     * @return 绝对路径
     */
    public Path getFilePath(String relativePath) {
        return rootLocation.resolve(relativePath).normalize();
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "请选择要上传的文件");
        }

        // 检查文件大小
        if (file.getSize() > maxFileSize) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED, 
                    "文件大小超过限制（最大 " + (maxFileSize / 1024 / 1024) + "MB）");
        }

        // 检查文件类型
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "文件名不能为空");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!allowedExtensions.contains(extension)) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED, 
                    "不支持的文件类型，允许的类型: " + allowedTypes);
        }

        // 检查 Content-Type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED, "只允许上传图片文件");
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }

    /**
     * 生成文件名
     * 格式: prefix-序号.extension 或 prefix-uuid.extension
     */
    private String generateFilename(String prefix, String extension) {
        if (StringUtils.hasText(prefix)) {
            // 查找现有文件，确定序号
            int index = findNextIndex(prefix);
            return String.format("%s-%02d.%s", prefix, index, extension);
        } else {
            // 使用 UUID
            return UUID.randomUUID().toString() + "." + extension;
        }
    }

    /**
     * 查找下一个可用的文件序号
     */
    private int findNextIndex(String prefix) {
        Path speciesDir = rootLocation.resolve("species");
        int maxIndex = 0;
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(speciesDir, prefix + "-*")) {
            for (Path path : stream) {
                String filename = path.getFileName().toString();
                // 提取序号，格式如 AQ0001-01.png
                int dashIndex = filename.lastIndexOf('-');
                int dotIndex = filename.lastIndexOf('.');
                if (dashIndex > 0 && dotIndex > dashIndex) {
                    try {
                        int index = Integer.parseInt(filename.substring(dashIndex + 1, dotIndex));
                        maxIndex = Math.max(maxIndex, index);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (IOException e) {
            log.warn("查找文件序号失败: {}", e.getMessage());
        }
        
        return maxIndex + 1;
    }
}
