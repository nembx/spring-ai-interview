package org.nembx.app.module.knowledge.repository;


import org.nembx.app.common.enums.SessionStatus;
import org.nembx.app.module.knowledge.entity.pojo.RagSession;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Lian
 */
@Repository
public interface RagSessionRepository extends JpaRepository<RagSession, Long> {
    @EntityGraph(attributePaths = "knowledges")
    List<RagSession> findAllByStatusOrderByUpdatedAtDesc(SessionStatus status);

    Optional<RagSession> findByIdAndStatus(Long id, SessionStatus status);

    @EntityGraph(attributePaths = "knowledges")
    Optional<RagSession> findWithKnowledgesById(Long id);

    @EntityGraph(attributePaths = "knowledges")
    Optional<RagSession> findWithKnowledgesByIdAndStatus(Long id, SessionStatus status);

    @Modifying
    @SuppressWarnings("SqlResolve")
    @Query(value = "DELETE FROM rag_session_knowledge WHERE knowledge_id = :knowledgeId", nativeQuery = true)
    void deleteKnowledgeRelations(Long knowledgeId);
}
