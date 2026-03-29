package org.nembx.app.module.knowledge.repository;


import org.nembx.app.module.knowledge.entity.pojo.RagMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Lian
 */

@Repository
public interface RagMessageRepository extends JpaRepository<RagMessage, Long> {
    List<RagMessage> findAllBySessionIdOrderByCreatedAtAsc(Long sessionId);

    List<RagMessage> findByCompletedFalseAndCreatedAtBefore(LocalDateTime threshold);
}
