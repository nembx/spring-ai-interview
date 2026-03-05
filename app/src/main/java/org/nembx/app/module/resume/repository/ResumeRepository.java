package org.nembx.app.module.resume.repository;


import org.nembx.app.common.enums.status.TaskStatus;
import org.nembx.app.module.resume.enity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Lian
 */
@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {
    Optional<Resume> findByFileHash(String fileHash);

    @Modifying
    @Query("update Resume set status = ?2 where id = ?1")
    Integer updateResumeTaskStatus(Long resumeId, TaskStatus taskStatus);
}
