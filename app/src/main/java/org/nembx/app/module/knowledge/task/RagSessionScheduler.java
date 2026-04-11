package org.nembx.app.module.knowledge.task;

import cn.hutool.core.collection.CollectionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.module.knowledge.entity.pojo.RagMessage;
import org.nembx.app.module.knowledge.repository.RagMessageRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Lian
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class RagSessionScheduler {
    private final RagMessageRepository ragMessageRepository;

    /**
     * 定时清理超时未完成的 assistant 消息，防止服务异常终止导致消息永远卡在"正在思考中..."
     */
    @Scheduled(fixedDelay = 300000) // 每5分钟执行一次
    @Transactional
    public void cleanupStaleMessages() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(10);
        List<RagMessage> staleMessages = ragMessageRepository.findByCompletedFalseAndCreatedAtBefore(threshold);
        if (CollectionUtil.isEmpty(staleMessages)) {
            return;
        }
        for (RagMessage msg : staleMessages) {
            msg.setCompleted(true).setContent("【超时】回答生成失败，请重试");
            ragMessageRepository.save(msg);
        }
        log.warn("清理超时未完成消息 {} 条", staleMessages.size());
    }
}
