package org.nembx.app.module.interview.repository;

import org.nembx.app.common.enums.SessionStatus;
import org.nembx.app.module.interview.entity.pojo.InterviewSession;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Lian
 */
@Repository
public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {
    List<InterviewSession> findAllByStatus(SessionStatus status);

    Optional<InterviewSession> findByIdAndStatus(Long id, SessionStatus status);

    @EntityGraph(attributePaths = "knowledges")
    Optional<InterviewSession> findWithKnowledgesByIdAndStatus(Long id, SessionStatus status);
}
