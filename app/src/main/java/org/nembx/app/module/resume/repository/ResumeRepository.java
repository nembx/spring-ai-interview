package org.nembx.app.module.resume.repository;


import org.nembx.app.module.resume.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Lian
 */
@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {
    Optional<Resume> findByFileHash(String fileHash);

//    @Modifying
//    @Query("update Resume r set r.status = :taskStatus where r.id = :resumeId")
//    Integer updateResumeTaskStatus(@Param("resumeId") Long resumeId, @Param("taskStatus") TaskStatus taskStatus);
}
