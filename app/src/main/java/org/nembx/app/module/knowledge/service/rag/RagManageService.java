package org.nembx.app.module.knowledge.service.rag;

import cn.hutool.core.collection.CollectionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Lian
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RagManageService {
    private final RagSessionRepository ragSessionRepository;

    private final RagMessageRepository ragMessageRepository;

    private final KnowledgeRepository knowledgeRepository;

    private final KnowledgeManageService knowledgeManageService;

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

    private String generateTitle(List<Knowledge> knowledgeList) {
        if (knowledgeList == null)
            return "新rag会话";
        if (knowledgeList.size() == 1)
            return knowledgeList.getFirst().getFileName() + " rag会话";
        return knowledgeList.size() + "个知识库的rag会话";
    }
}
