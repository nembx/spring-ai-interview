package org.nembx.app.module.resume.entity.res;


import org.nembx.app.module.resume.entity.pojo.Resume;

/**
 * @author Lian
 */
public record ExportParamResponse(
        Resume resume,
        ResumeDetailResponse resumeDetailResponse
) {
}
