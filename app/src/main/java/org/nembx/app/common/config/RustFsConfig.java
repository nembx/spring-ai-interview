package org.nembx.app.common.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

/**
 * @author Lian
 */

/**
 * 配置类，用于创建和配置S3客户端实例。
 * 该类通过读取RustFsProperties配置属性来构建S3客户端，
 * 支持自定义端点、区域和认证信息。
 */
@Configuration
@RequiredArgsConstructor
public class RustFsConfig {
    private final RustFsProperties rustFsProperties;

    /**
     * 创建并配置S3客户端Bean。
     * 
     * @return 配置好的S3Client实例
     */
    @Bean
    public S3Client s3Client() {
        // 设置S3服务的自定义端点URL
        URI endpoint = URI.create(rustFsProperties.getEndpoint());
        
        // 设置S3服务所在的区域
        Region region = Region.of(rustFsProperties.getRegion());
        
        // 使用访问密钥和秘密密钥创建静态凭证提供者
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
            rustFsProperties.getAccessKey(),
            rustFsProperties.getSecretKey()
        );
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);
        
        // 构建并返回S3客户端实例
        return S3Client.builder()
                .endpointOverride(endpoint)
                .region(region)
                .credentialsProvider(credentialsProvider)
                .forcePathStyle(true)
                .build();
    }
}
