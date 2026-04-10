package org.nembx.app.module.knowledge.service.knowledge;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.properties.RerankProperties;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;

/**
 * @author Lian
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeRerankService {
    private final RestClient.Builder restClientBuilder;

    private final RerankProperties rerankProperties;

    private record RerankRequest(
            String model,
            String query,
            List<String> documents
    ) {
    }

    private record RerankResponse(
            @JsonAlias({"results", "data"}) List<RerankResult> results
    ) {
    }

    private record RerankResult(
            Integer index,
            @JsonAlias({"relevance_score", "score"}) Double relevanceScore
    ) {
    }

    public List<Document> rerank(String question, List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return List.of();
        }
        try {
            RestClient restClient = restClientBuilder.build();
            RerankResponse response = restClient.post()
                    .uri(rerankProperties.getBaseUrl())
                    .header("Authorization", "Bearer " + rerankProperties.getApiKey())
                    .body(new RerankRequest(
                            rerankProperties.getModel(),
                            question,
                            documents.stream().map(Document::getText).toList()
                    ))
                    .retrieve()
                    .body(RerankResponse.class);
            if (response == null || response.results() == null || response.results().isEmpty()) {
                return fallback(documents);
            }
            // 开始重排序
            List<Document> rerankedDocuments = response.results().stream()
                    .map(RerankResult::index)
                    .filter(Objects::nonNull)
                    .filter(index -> index >= 0 && index < documents.size())
                    .map(documents::get)
                    .distinct()
                    .limit(rerankProperties.getFinalTopK())
                    .toList();
            return rerankedDocuments.isEmpty() ? fallback(documents) : rerankedDocuments;
        } catch (Exception e) {
            log.warn("Rerank失败, 降级为原始召回顺序: {}", e.getMessage());
            return fallback(documents);
        }
    }

    private List<Document> fallback(List<Document> documents) {
        return documents.stream()
                .limit(rerankProperties.getFinalTopK())
                .toList();
    }
}
