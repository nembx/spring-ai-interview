package org.nembx.app.module.resume.repository;


import org.nembx.app.module.resume.enity.ResumeAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Lian
 */
@Repository
public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, Long> {
    ResumeAnalysis findAnalysisByResumeId(Long resumeId);
}
