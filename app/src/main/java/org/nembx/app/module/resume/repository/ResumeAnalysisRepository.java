package org.nembx.app.module.resume.repository;


import org.nembx.app.module.resume.entity.pojo.ResumeAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Lian
 */
@Repository
public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, Long> {
    ResumeAnalysis findFirstByResumeIdOrderByAnalyzedAtDesc(Long resumeId);

    List<ResumeAnalysis> findAllByResumeId(Long resumeId);
}
