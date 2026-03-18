package org.nembx.app.module.knowledge.repository;


import org.nembx.app.module.knowledge.entity.Knowledge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Lian
 */

@Repository
public interface KnowledgeRepository extends JpaRepository<Knowledge, Long> {
    Optional<Knowledge> findKnowledgeByFileHash(String fileHash);

    List<Knowledge> findAllByOrderByUploadTimeDesc();

    List<Knowledge> findAllByCategory(String category);

//    @Modifying
//    @Query("update Knowledge set taskStatus = :status where id = :id")
//    Integer updateKnowledgeStatus(@Param("status") TaskStatus status, @Param("id") Long id);
}
