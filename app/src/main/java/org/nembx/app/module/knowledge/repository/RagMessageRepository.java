package org.nembx.app.module.knowledge.repository;


import org.nembx.app.module.knowledge.enity.RagMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Lian
 */

@Repository
public interface RagMessageRepository extends JpaRepository<RagMessage, Long> {
    List<RagMessage> findAllBySessionIdOrderByCreatedAtAsc(Long sessionId);
}
