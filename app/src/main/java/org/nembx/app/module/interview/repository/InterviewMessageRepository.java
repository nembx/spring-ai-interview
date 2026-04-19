package org.nembx.app.module.interview.repository;

import org.nembx.app.module.interview.entity.pojo.InterviewMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Lian
 */
@Repository
public interface InterviewMessageRepository extends JpaRepository<InterviewMessage, Long> {
    List<InterviewMessage> findAllBySessionIdOrderByCreatedAtAsc(Long sessionId);
}
