package org.nembx.app.module.knowledge.repository;


import org.nembx.app.module.knowledge.enity.RagSession;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Lian
 */
@Repository
public interface RagSessionRepository extends JpaRepository<RagSession, Long> {
    @EntityGraph(attributePaths = "knowledges")
    List<RagSession> findAllByOrderByUpdatedAtDesc();

    @EntityGraph(attributePaths = "knowledges")
    Optional<RagSession> findWithKnowledgesById(Long id);
}
