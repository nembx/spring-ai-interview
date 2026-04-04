package org.nembx.app.common.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.exception.BusinessException;
import org.nembx.app.common.exception.ErrorCode;
import org.nembx.app.common.properties.RustFsProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Lian
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class RustFsService implements FileStorage {
    private final S3Client s3Client;

    private final RustFsProperties rustFsProperties;

    @PostConstruct
    public void init() {
        log.info("初始化 RustFs服务");
        ensureBucketExists();
    }

    public boolean notExists(String fileKey) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(rustFsProperties.getBucket())
                    .key(fileKey)
                    .build();
            s3Client.headObject(headRequest);
            return false;
        } catch (NoSuchKeyException e) {
            return true;
        } catch (S3Exception e) {
            log.warn("检查文件存在性失败: {} - {}", fileKey, e.getMessage());
            return true;
        }
    }

    @Override
    public String upload(MultipartFile file, String namespace) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件名不能为空");
        }
        // 防止路径遍历：只取文件名部分，去除目录路径
        originalFilename = originalFilename.replace("\\", "/");
        if (originalFilename.contains("/")) {
            originalFilename = originalFilename.substring(originalFilename.lastIndexOf("/") + 1);
        }
        String key = namespace + "/" + originalFilename;
        // 创建存储桶
        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(rustFsProperties.getBucket())
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
            log.info("上传文件成功, 文件名为：{}", originalFilename);
            return key;
        } catch (IOException e) {
            log.error("文件读取失败, 原因是: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "上传失败");
        } catch (S3Exception e) {
            log.error("上传文件失败, 原因为: {}", e.getMessage());
            throw new BusinessException(ErrorCode.UPLOAD_FAIL, "上传失败");
        }
    }

    @Override
    public void delete(String key) {
        if (key == null) {
            log.debug("删除文件失败, 文件键为空");
            return;
        }
        if (notExists(key)) {
            log.warn("删除文件失败, 文件不存在");
            return;
        }
        try {
            s3Client.deleteObject(builder -> builder.bucket(rustFsProperties.getBucket()).key(key));
            log.info("删除文件成功, 文件名为：{}", key);
        } catch (S3Exception e) {
            log.error("删除文件失败, 原因为: {}", e.getMessage());
            throw new BusinessException(ErrorCode.DELETE_FAIL, "删除失败");
        }
    }

    @Override
    public InputStream download(String key) {
        if (key == null || notExists(key)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文件不存在");
        }
        try {
            // getObject 返回的是一个流，不会一次性加载到内存
            return s3Client.getObject(builder ->
                    builder.bucket(rustFsProperties.getBucket()).key(key));
        } catch (S3Exception e) {
            log.error("获取文件流失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.DOWNLOAD_FAIL, "文件提取失败");
        }
    }

    private void ensureBucketExists() {
        try {
            s3Client.headBucket(builder -> builder.bucket(rustFsProperties.getBucket()));
            log.info("存储桶已存在: {}", rustFsProperties.getBucket());
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                log.info("创建存储桶: {}", rustFsProperties.getBucket());
                s3Client.createBucket(builder -> builder.bucket(rustFsProperties.getBucket()));
            } else {
                log.error("创建存储桶失败: {}", e.getMessage());
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "存储服务不可用: " + e.getMessage());
            }
        }
    }

    @Override
    public String getUrl(String key) {
        if (key == null || notExists(key)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文件不存在");
        }
        return s3Client.utilities().getUrl(builder ->
                builder.bucket(rustFsProperties.getBucket()).key(key)).toString();
    }
}
