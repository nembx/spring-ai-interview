package org.nembx.app.module.interview.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.ai.AiClient;
import org.nembx.app.common.entity.dto.PreparedChatContext;
import org.nembx.app.common.entity.dto.RetrievalContext;
import org.nembx.app.common.enums.MessageType;
import org.nembx.app.common.enums.SessionStatus;
import org.nembx.app.common.exception.BusinessException;
import org.nembx.app.common.exception.ErrorCode;
import org.nembx.app.module.interview.entity.pojo.InterviewMessage;
import org.nembx.app.module.interview.entity.pojo.InterviewSession;
import org.nembx.app.module.interview.repository.InterviewMessageRepository;
import org.nembx.app.module.interview.repository.InterviewSessionRepository;
import org.nembx.app.module.resume.entity.pojo.Resume;
import org.nembx.app.module.resume.service.resume.ResumeManageService;
import org.springframework.core.io.Resource;
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
public class InterviewChatService {
    private final InterviewMessageRepository interviewMessageRepository;

    private final InterviewSessionRepository interviewSessionRepository;

    private final AudioService audioService;

    private final AiClient aiClient;

    private final ResumeManageService resumeManageService;

    private final InterviewRetrieveService interviewRetrieveService;

    private final TransactionTemplate transactionTemplate;

    /**
     * 文本对话 — SSE 流式输出
     */
    public Flux<ServerSentEvent<String>> chat(Long sessionId, String question) {
        PreparedChatContext ctx = transactionTemplate.execute(status -> prepareChat(sessionId, question));
        if (ctx == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "初始化interview会话消息失败");
        }

        StringBuffer content = new StringBuffer();
        return Flux.defer(() -> chatFlux(sessionId, question, ctx.userMessageId()))
                .doOnNext(content::append)
                .map(token -> ServerSentEvent.<String>builder()
                        .data(token.replace("\n", "\\n")
                                .replace("\r", "\\r"))
                        .build())
                .doOnComplete(() -> {
                    transactionTemplate.executeWithoutResult(status -> completeChat(ctx.assistantMessageId(), content.toString()));
                    log.info("[面试文本流完成] sessionId: {}, messageId: {}", sessionId, ctx.assistantMessageId());
                })
                .doOnCancel(() -> {
                    String partial = content.toString();
                    String cancelContent = partial.isEmpty() ? "【中断】客户端断开连接" : partial;
                    transactionTemplate.executeWithoutResult(status -> completeChat(ctx.assistantMessageId(), cancelContent));
                    log.warn("[面试文本流中断] sessionId: {}, messageId: {}, 已接收{}字符",
                            sessionId, ctx.assistantMessageId(), partial.length());
                })
                .doOnError(throwable -> {
                    String errorContent = !content.isEmpty()
                            ? content.toString() : "【错误】回答生成失败：" + throwable.getMessage();
                    transactionTemplate.executeWithoutResult(status -> completeChat(ctx.assistantMessageId(), errorContent));
                    log.error("[面试文本流错误]", throwable);
                });
    }

    /**
     * 语音对话 — 语音进，文本出
     * 流程：Audio → STT → AI → Text（前端用 Web Speech API 朗读）
     */
    public Flux<byte[]> voiceChat(Long sessionId, Resource audioFile) {
        // 语音转文本
        String question = audioService.speechToText(audioFile);
        log.info("[STT完成] sessionId: {}, 识别文本: {}", sessionId, question);

        // 预处理：保存消息、更新题数
        PreparedChatContext ctx = transactionTemplate.execute(status -> prepareChat(sessionId, question));
        if (ctx == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "初始化interview会话消息失败");
        }

        // AI 同步调用获取完整回复
        String aiReply = getAiReply(sessionId, question, ctx.userMessageId());

        // 持久化 assistant 消息
        transactionTemplate.executeWithoutResult(status -> completeChat(ctx.assistantMessageId(), aiReply));
        log.info("[面试语音回复] sessionId: {}, 回复: {}", sessionId, aiReply);

        return audioService.textToSpeech(aiReply);
    }

    private PreparedChatContext prepareChat(Long sessionId, String question) {
        InterviewSession session = interviewSessionRepository.findByIdAndStatus(sessionId, SessionStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "interview会话不存在或已结束"));

        // 保存 user 消息
        InterviewMessage userMessage = new InterviewMessage();
        userMessage.setSessionId(sessionId)
                .setType(MessageType.USER)
                .setContent(question)
                .setCompleted(true);
        interviewMessageRepository.save(userMessage);

        // 保存 assistant 占位消息
        InterviewMessage assistantMessage = new InterviewMessage();
        assistantMessage.setSessionId(sessionId)
                .setType(MessageType.ASSISTANT)
                .setContent("正在思考中...")
                .setCompleted(false);
        interviewMessageRepository.save(assistantMessage);

        // 更新已提问数
        session.setQuestionCount(session.getQuestionCount() + 1);
        interviewSessionRepository.save(session);

        log.info("[面试消息预处理] sessionId: {}, 第{}/{}题, userMsgId: {}, assistantMsgId: {}",
                sessionId, session.getQuestionCount(), session.getMaxQuestions(),
                userMessage.getId(), assistantMessage.getId());

        return new PreparedChatContext(userMessage.getId(), assistantMessage.getId());
    }

    private Flux<String> chatFlux(Long sessionId, String question, Long currentUserMessageId) {
        RetrievalContext retrievalContext = buildRetrievalContext(sessionId, question, currentUserMessageId);
        return aiClient.stream(retrievalContext.systemPrompt(), retrievalContext.userPrompt())
                .doOnNext(token -> log.debug("[面试流式输出] {}", token))
                .doOnComplete(() -> log.info("[面试流式输出完成]"))
                .doOnError(e -> log.error("[面试流式输出失败]", e));
    }

    private String getAiReply(Long sessionId, String question, Long currentUserMessageId) {
        RetrievalContext retrievalContext = buildRetrievalContext(sessionId, question, currentUserMessageId);
        return aiClient.call(retrievalContext.systemPrompt(), retrievalContext.userPrompt());
    }

    private RetrievalContext buildRetrievalContext(Long sessionId, String question, Long currentUserMessageId) {
        InterviewSession session = interviewSessionRepository.findWithKnowledgesByIdAndStatus(sessionId, SessionStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "interview会话不存在"));
        Resume resume = resumeManageService.getOneById(session.getResumeId());

        List<InterviewMessage> messages = interviewMessageRepository.findAllBySessionIdOrderByCreatedAtAsc(sessionId)
                .stream()
                .filter(msg -> Boolean.TRUE.equals(msg.getCompleted()))
                .filter(msg -> !msg.getId().equals(currentUserMessageId))
                .toList();

        return interviewRetrieveService.retrieve(
                session.getJdContent(), question, resume.getContent(),
                session.getQuestionCount(), session.getMaxQuestions(), messages,
                session.getKnowledgeIds());
    }

    private void completeChat(Long messageId, String content) {
        InterviewMessage message = interviewMessageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "interview消息不存在"));
        message.setCompleted(true)
                .setContent(content);
        log.info("[面试消息完成] messageId: {}", messageId);
    }
}
