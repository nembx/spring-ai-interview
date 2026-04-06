package org.nembx.app.common.config;

import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Lian
 */

@Configuration
public class KnowledgeTextSplitter {

    @Bean
    TextSplitter textSplitter() {
        return TokenTextSplitter.builder()
                .withChunkSize(500)
                .withMinChunkSizeChars(200)
                .withMinChunkLengthToEmbed(20)
                .withMaxNumChunks(2000)
                .withKeepSeparator(true)
                .build();
    }
}
