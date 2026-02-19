package org.nembx.app.common.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Lian
 */

@Data
@Component
@ConfigurationProperties(prefix = "app.storage")
public class RustFsProperties {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String region;
}
