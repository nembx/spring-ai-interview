package org.nembx.app.module.knowledge.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Lian
 */
@Data
@Component
@ConfigurationProperties(prefix = "vector")
public class VectorProperties {
    private Integer topK;
    private Double minScore;
}
