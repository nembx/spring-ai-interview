package org.nembx.app.module.knowledge.service;


import cn.hutool.core.collection.CollectionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.enums.MessageType;
import org.nembx.app.common.enums.SessionStatus;
import org.nembx.app.common.exception.BusinessException;
import org.nembx.app.common.exception.ErrorCode;
import org.nembx.app.module.knowledge.entity.dto.RagMessageDTO;
import org.nembx.app.module.knowledge.entity.pojo.Knowledge;
import org.nembx.app.module.knowledge.entity.pojo.RagMessage;
import org.nembx.app.module.knowledge.entity.pojo.RagSession;
import org.nembx.app.module.knowledge.entity.req.CreateSessionRequest;
import org.nembx.app.module.knowledge.entity.res.RagSessionDetailResponse;
import org.nembx.app.module.knowledge.entity.res.RagSessionResponse;
import org.nembx.app.module.knowledge.repository.KnowledgeRepository;
import org.nembx.app.module.knowledge.repository.RagMessageRepository;
import org.nembx.app.module.knowledge.repository.RagSessionRepository;
import org.nembx.app.module.knowledge.service.knowledge.KnowledgeManageService;
import org.nembx.app.module.knowledge.service.knowledge.KnowledgeQueryService;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Lian
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class RagChatService {
    private record PreparedChatContext(Long userMessageId, Long assistantMessageId) {
    }

    private final RagSessionRepository ragSessionRepository;

    private final RagMessageRepository ragMessageRepository;

    private final KnowledgeRepository knowledgeRepository;

    private final KnowledgeManageService knowledgeManageService;

    private final KnowledgeQueryService knowledgeQueryService;

    private final TransactionTemplate transactionTemplate;

    @Transactional(rollbackFor = Exception.class)
    public RagSessionResponse createSession(CreateSessionRequest request) {
        List<Long> knowledgeIds = request.knowledgeIds();
        if (CollectionUtil.isEmpty(knowledgeIds)) {
            log.warn("创建rag会话失败, 知识ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR, "知识ID为空");
        }
        List<Knowledge> knowledgeList = knowledgeRepository.findAllById(knowledgeIds);
        if (CollectionUtil.isEmpty(knowledgeList)) {
            log.warn("创建rag会话失败, 知识库不存在");
            throw new BusinessException(ErrorCode.NOT_FOUND, "知识库不存在");
        }

        RagSession ragSession = new RagSession();
        ragSession.setTitle(request.title() != null && !request.title().isBlank()
                        ? request.title() : generateTitle(knowledgeList))
                .setKnowledges(knowledgeList);

        ragSessionRepository.save(ragSession);
        log.info("创建rag会话成功, 会话ID: {}", ragSession.getId());
        return new RagSessionResponse(ragSession.getId(),
                ragSession.getTitle(),
                knowledgeIds,
                ragSession.getStatus(),
                ragSession.getCreatedAt());
    }

    public List<RagSessionResponse> getSessionsByStatus(SessionStatus status) {
        List<RagSession> ragSessions = ragSessionRepository.findAllByStatusOrderByUpdatedAtDesc(status);
        return ragSessions.stream().map(
                ragSession -> new RagSessionResponse(ragSession.getId(),
                        ragSession.getTitle(),
                        ragSession.getKnowledges().stream().map(Knowledge::getId).toList(),
                        ragSession.getStatus(),
                        ragSession.getCreatedAt())
        ).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteSession(Long sessionId) {
        if (!ragSessionRepository.existsById(sessionId)) {
            log.warn("删除rag会话失败, 会话ID不存在");
            throw new BusinessException(ErrorCode.NOT_FOUND, "rag会话不存在");
        }
        ragSessionRepository.deleteById(sessionId);
        log.info("删除rag会话成功, 会话ID: {}", sessionId);
    }

    public RagSessionDetailResponse getSessionDetail(Long sessionId) {
        RagSession ragSession = ragSessionRepository.findWithKnowledgesById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "rag会话不存在"));
        List<RagMessage> messages = ragMessageRepository.findAllBySessionIdOrderByCreatedAtAsc(sessionId);
        List<RagMessageDTO> listDTO = messages.stream().map(
                ragMessage -> new RagMessageDTO(ragMessage.getId(),
                        ragMessage.getType(),
                        ragMessage.getContent())
        ).toList();
        log.info("获取rag会话详情成功, 会话ID: {}, 会话标题: {}, 知识库数量: {}, 会话创建时间: {}, 会话更新时间: {}",
                ragSession.getId(), ragSession.getTitle(), ragSession.getKnowledges().size(), ragSession.getCreatedAt(), ragSession.getUpdatedAt());
        return new RagSessionDetailResponse(ragSession.getId(),
                ragSession.getTitle(),
                knowledgeManageService.toListDTO(ragSession.getKnowledges()),
                listDTO,
                ragSession.getStatus(),
                ragSession.getCreatedAt(),
                ragSession.getUpdatedAt()
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateSessionKnowledge(Long sessionId, List<Long> knowledgeIds) {
        if (CollectionUtil.isEmpty(knowledgeIds)) {
            log.warn("更新rag会话知识失败, 知识ID为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR, "知识ID为空");
        }
        RagSession ragSession = ragSessionRepository.findByIdAndStatus(sessionId, SessionStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "rag会话不存在"));
        List<Knowledge> knowledgeList = knowledgeRepository.findAllById(knowledgeIds);
        ragSession.setKnowledges(knowledgeList);
        ragSessionRepository.save(ragSession);
        log.info("更新rag会话知识成功, 会话ID: {}", sessionId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateSessionTitle(Long sessionId, String title) {
        if (title == null || title.isBlank())
            throw new BusinessException(ErrorCode.PARAM_ERROR, "标题不能为空");
        RagSession ragSession = ragSessionRepository.findByIdAndStatus(sessionId, SessionStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "rag会话不存在"));
        ragSession.setTitle(title);
        log.info("更新rag会话标题成功, 会话ID: {}", sessionId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateSessionStatus(Long sessionId, SessionStatus targetStatus) {
        if (targetStatus == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "会话状态不能为空");
        }
        RagSession ragSession = ragSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "rag会话不存在"));
        SessionStatus currentStatus = ragSession.getStatus();
        if (currentStatus == targetStatus) {
            log.info("rag会话状态无需更新, 会话ID: {}, 状态: {}", sessionId, targetStatus);
            return;
        }
        ragSession.setStatus(targetStatus);
        log.info("更新rag会话状态成功, 会话ID: {}, {} -> {}", sessionId, currentStatus, targetStatus);
    }

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

    private String generateTitle(List<Knowledge> knowledgeList) {
        if (knowledgeList == null)
            return "新rag会话";
        if (knowledgeList.size() == 1)
            return knowledgeList.getFirst().getFileName() + " rag会话";
        return knowledgeList.size() + "个知识库的rag会话";
    }

    /**
     * 定时清理超时未完成的 assistant 消息，防止服务异常终止导致消息永远卡在"正在思考中..."
     */
    @Scheduled(fixedDelay = 300000) // 每5分钟执行一次
    @Transactional
    public void cleanupStaleMessages() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(10);
        List<RagMessage> staleMessages = ragMessageRepository.findByCompletedFalseAndCreatedAtBefore(threshold);
        if (staleMessages.isEmpty()) {
            return;
        }
        for (RagMessage msg : staleMessages) {
            msg.setCompleted(true).setContent("【超时】回答生成失败，请重试");
            ragMessageRepository.save(msg);
        }
        log.warn("清理超时未完成消息 {} 条", staleMessages.size());
    }
}
