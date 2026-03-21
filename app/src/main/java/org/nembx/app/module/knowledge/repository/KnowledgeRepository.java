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

    Optional<Knowledge> findFirstByFileNameAndCategoryOrderByUploadTimeDesc(String fileName, String category);
}
