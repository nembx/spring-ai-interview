package org.nembx.app.module.resume.repository;


import org.nembx.app.module.resume.enity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Lian
 */
@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {
    Optional<Resume> findByFileHash(String fileHash);
}
