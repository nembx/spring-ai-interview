package org.nembx.app.module.resume.repository;


import org.nembx.app.module.resume.entity.pojo.JdMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Lian
 */
@Repository
public interface JdMatchRepository extends JpaRepository<JdMatch, Long> {
    Optional<JdMatch> findByResumeId(Long resumeId);
}
