package org.nembx.app.module.interview.service;

import cn.hutool.core.collection.CollectionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.enums.SessionStatus;
import org.nembx.app.common.exception.BusinessException;
import org.nembx.app.common.exception.ErrorCode;
import org.nembx.app.module.interview.entity.dto.InterviewMessageDTO;
import org.nembx.app.module.interview.entity.dto.SkillSelectionResult;
import org.nembx.app.module.interview.entity.pojo.InterviewMessage;
import org.nembx.app.module.interview.entity.pojo.InterviewSession;
import org.nembx.app.module.interview.entity.req.CreateInterviewSessionRequest;
import org.nembx.app.module.interview.entity.res.InterviewSessionDetailResponse;
import org.nembx.app.module.interview.entity.res.InterviewSessionResponse;
import org.nembx.app.module.interview.repository.InterviewMessageRepository;
import org.nembx.app.module.interview.repository.InterviewSessionRepository;
import org.nembx.app.module.knowledge.entity.pojo.Knowledge;
import org.nembx.app.module.knowledge.repository.KnowledgeRepository;
import org.nembx.app.module.resume.entity.pojo.Resume;
import org.nembx.app.module.resume.service.resume.ResumeManageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Lian
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InterviewManageService {
    private final InterviewMessageRepository interviewMessageRepository;

    private final InterviewSessionRepository interviewSessionRepository;

    private final KnowledgeRepository knowledgeRepository;

    private final ResumeManageService resumeManageService;

    private final InterviewSkillRouter interviewSkillRouter;

    @Transactional(rollbackFor = Exception.class)
    public InterviewSessionResponse createInterviewSession(CreateInterviewSessionRequest createInterviewSessionRequest) {
        if (createInterviewSessionRequest.resumeId() == null ||
                createInterviewSessionRequest.jdContent() == null) {
            log.warn("创建interview会话失败, 简历ID为空或职位描述");
            throw new BusinessException(ErrorCode.PARAM_ERROR, "简历ID或职位描述为空");
        }

        Resume resume = resumeManageService.getOneById(createInterviewSessionRequest.resumeId());
        SkillSelectionResult skillResult = interviewSkillRouter.route(
                createInterviewSessionRequest.jdContent(), resume.getContent());

        InterviewSession interviewSession = new InterviewSession();
        interviewSession.setResumeId(createInterviewSessionRequest.resumeId())
                .setJdContent(createInterviewSessionRequest.jdContent())
                .setTitle(createInterviewSessionRequest.title() == null ?
                        "默认会话" : createInterviewSessionRequest.title())
                .setSelectedSkill(skillResult.skill())
                .setSkillReason(skillResult.reason());

        List<Long> knowledgeIds = createInterviewSessionRequest.knowledgeIds();
        if (CollectionUtil.isNotEmpty(knowledgeIds)) {
            List<Knowledge> knowledgeList = knowledgeRepository.findAllById(knowledgeIds);
            if (CollectionUtil.isNotEmpty(knowledgeList)) {
                interviewSession.setKnowledges(knowledgeList);
            }
        }

        interviewSessionRepository.save(interviewSession);
        return new InterviewSessionResponse(
                interviewSession.getId(),
                interviewSession.getTitle(),
                interviewSession.getJdContent(),
                interviewSession.getKnowledgeIds(),
                interviewSession.getSelectedSkill(),
                interviewSession.getStatus(),
                interviewSession.getCreatedAt()
        );
    }

    public List<InterviewSessionResponse> getSessionsByStatus(SessionStatus status) {
        List<InterviewSession> interviewSessions = interviewSessionRepository.findAllByStatus(status);
        return interviewSessions.stream().map(
                ragSession -> new InterviewSessionResponse(ragSession.getId(),
                        ragSession.getTitle(),
                        ragSession.getJdContent(),
                        ragSession.getKnowledgeIds(),
                        ragSession.getSelectedSkill(),
                        ragSession.getStatus(),
                        ragSession.getCreatedAt())
        ).toList();
    }

    public InterviewSessionDetailResponse getSessionDetail(Long sessionId) {
        InterviewSession interviewSession = interviewSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "interview会话不存在"));
        List<InterviewMessage> messages = interviewMessageRepository.findAllBySessionIdOrderByCreatedAtAsc(sessionId);
        List<InterviewMessageDTO> listDTO = messages.stream().map(
                interviewMessage -> new InterviewMessageDTO(
                        interviewMessage.getId(),
                        interviewMessage.getType(),
                        interviewMessage.getContent())
        ).toList();
        log.info("获取interview会话详情成功, 会话ID: {}, 会话标题: {}, 会话创建时间: {}, 会话更新时间: {}",
                interviewSession.getId(), interviewSession.getTitle(),
                interviewSession.getCreatedAt(), interviewSession.getUpdatedAt());

        return new InterviewSessionDetailResponse(
                interviewSession.getId(),
                interviewSession.getTitle(),
                interviewSession.getKnowledgeIds(),
                listDTO,
                interviewSession.getStatus(),
                interviewSession.getCreatedAt(),
                interviewSession.getUpdatedAt()
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteSession(Long sessionId) {
        if (!interviewSessionRepository.existsById(sessionId)) {
            log.warn("删除interview会话失败, 会话ID不存在");
            throw new BusinessException(ErrorCode.NOT_FOUND, "interview会话不存在");
        }
        interviewSessionRepository.deleteById(sessionId);
        log.info("删除interview会话成功, 会话ID: {}", sessionId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateSessionStatus(Long sessionId, SessionStatus status) {
        InterviewSession session = interviewSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "interview会话不存在"));
        session.setStatus(status);
        log.info("更新interview会话状态成功, 会话ID: {}, 状态: {}", sessionId, status);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateSessionTitle(Long sessionId, String title) {
        InterviewSession session = interviewSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "interview会话不存在"));
        session.setTitle(title);
        log.info("更新interview会话标题成功, 会话ID: {}, 标题: {}", sessionId, title);
    }
}
