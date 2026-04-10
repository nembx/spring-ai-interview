package org.nembx.app.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Lian
 */
@Data
@Component
@ConfigurationProperties(prefix = "rerank")
public class RerankProperties {
    private String baseUrl;
    private String apiKey;
    private String model;
    private Integer finalTopK;
}
