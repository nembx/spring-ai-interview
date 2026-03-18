package org.nembx.app.module.knowledge.listener;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.module.knowledge.entity.dto.KnowledgeListenerDTO;
import org.nembx.app.module.knowledge.service.knowledge.KnowledgeVectorService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * @author Lian
 */

@Component
@Slf4j
@RequiredArgsConstructor
public class KnowledgeListener {
    private final KnowledgeVectorService knowledgeVectorService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApplicationEvent(KnowledgeListenerDTO knowledgeDTO) {
        try {
            Long id = knowledgeDTO.id();
            log.info("收到知识, Id为： {}", id);
            knowledgeVectorService.vectorizeKnowledge(id, knowledgeDTO.content());
        } catch (Exception e) {
            log.error("向量化知识失败: {}", e.getMessage());
        }
    }
}
