package org.nembx.app.module.knowledge.service.rag;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.entity.dto.PreparedChatContext;
import org.nembx.app.common.enums.MessageType;
import org.nembx.app.common.enums.SessionStatus;
import org.nembx.app.common.exception.BusinessException;
import org.nembx.app.common.exception.ErrorCode;
import org.nembx.app.module.knowledge.entity.pojo.RagMessage;
import org.nembx.app.module.knowledge.entity.pojo.RagSession;
import org.nembx.app.module.knowledge.repository.RagMessageRepository;
import org.nembx.app.module.knowledge.repository.RagSessionRepository;
import org.nembx.app.module.knowledge.service.knowledge.KnowledgeQueryService;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @author Lian
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class RagChatService {
    private final RagSessionRepository ragSessionRepository;

    private final RagMessageRepository ragMessageRepository;

    private final KnowledgeQueryService knowledgeQueryService;

    private final TransactionTemplate transactionTemplate;


    public Flux<ServerSentEvent<String>> chat(Long sessionId, String question) {
        // 预处理会话（编程式事务，确保在 Flux 订阅前提交）
        PreparedChatContext preparedChatContext = transactionTemplate.execute(status -> prepareChat(sessionId, question));
        if (preparedChatContext == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "初始化rag会话消息失败");
        }

        // 生成流式文本
        StringBuffer content = new StringBuffer();
        return Flux.defer(() -> chatFlux(sessionId, question, preparedChatContext.userMessageId()))
                .doOnNext(content::append)
                // 将流式文本转换为SSE格式
                .map(token -> ServerSentEvent.<String>builder()
                        .data(token.replace("\n", "\\n")
                                .replace("\r", "\\r"))
                        .build())
                .doOnComplete(() -> {
                    transactionTemplate.executeWithoutResult(status -> completeChat(preparedChatContext.assistantMessageId(), content.toString()));
                    log.info("[流式输出完成], sessionId为：{}, messageID为：{}", sessionId, preparedChatContext.assistantMessageId());
                })
                .doOnCancel(() -> {
                    String partial = content.toString();
                    String cancelContent = partial.isEmpty() ? "【中断】客户端断开连接" : partial;
                    transactionTemplate.executeWithoutResult(status -> completeChat(preparedChatContext.assistantMessageId(), cancelContent));
                    log.warn("[流式输出中断] sessionId: {}, messageId: {}, 已接收{}字符",
                            sessionId, preparedChatContext.assistantMessageId(), partial.length());
                })
                .doOnError(throwable -> {
                    String errorContent = !content.isEmpty()
                            ? content.toString() : "【错误】回答生成失败：" + throwable.getMessage();
                    transactionTemplate.executeWithoutResult(status -> completeChat(preparedChatContext.assistantMessageId(), errorContent));
                    log.error("[流式输出错误]", throwable);
                });
    }

    // 由编程式事务管理
    private PreparedChatContext prepareChat(Long sessionId, String question) {
        RagSession ragSession = ragSessionRepository.findByIdAndStatus(sessionId, SessionStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "rag会话不存在"));
        Integer count = ragSession.getMessageCount();
        // 生成user消息
        RagMessage userMessage = new RagMessage();
        userMessage.setSessionId(sessionId)
                .setType(MessageType.USER)
                .setContent(question)
                .setCompleted(true);
        ragMessageRepository.save(userMessage);
        // 生成assistant消息
        RagMessage assistantMessage = new RagMessage();
        assistantMessage.setSessionId(sessionId)
                .setType(MessageType.ASSISTANT)
                .setContent("正在思考中...")
                .setCompleted(false);
        ragMessageRepository.save(assistantMessage);
        // 更新rag会话消息数量
        ragSession.setMessageCount(count + 2);
        ragSessionRepository.save(ragSession);
        log.info("生成rag会话消息成功, 会话ID: {}, 消息ID: {}, {}", sessionId, userMessage.getId(), assistantMessage.getId());
        return new PreparedChatContext(userMessage.getId(), assistantMessage.getId());
    }

    private Flux<String> chatFlux(Long sessionId, String question, Long currentUserMessageId) {
        RagSession ragSession = ragSessionRepository.findWithKnowledgesByIdAndStatus(sessionId, SessionStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "rag会话不存在"));
        List<Long> knowledgeIds = ragSession.getKnowledgeIds();
        List<RagMessage> messages = ragMessageRepository.findAllBySessionIdOrderByCreatedAtAsc(sessionId)
                .stream()
                .filter(message -> Boolean.TRUE.equals(message.getCompleted()))
                .filter(message -> !message.getId().equals(currentUserMessageId))
                .toList();
        return knowledgeQueryService.answerQuestionStream(knowledgeIds, question, messages);
    }


    // 由编程式事务管理
    private void completeChat(Long messageId, String content) {
        RagMessage ragMessage = ragMessageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "rag会话消息不存在"));
        ragMessage.setCompleted(true)
                .setContent(content);
        log.info("完成rag会话消息成功, messageID: {}", messageId);
    }
}
