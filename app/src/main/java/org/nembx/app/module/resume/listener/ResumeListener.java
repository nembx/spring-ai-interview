package org.nembx.app.module.resume.listener;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.module.resume.enity.record.ResumeAnalysisResponse;
import org.nembx.app.module.resume.enity.record.ResumeDTO;
import org.nembx.app.module.resume.service.ResumeAiService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * @author Lian
 */

@Component
@Slf4j
@RequiredArgsConstructor
public class ResumeListener {
    private final ResumeAiService resumeAiService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApplicationEvent(ResumeDTO resumeDTO) {
        Long id = resumeDTO.resumeId();
        log.info("收到简历, Id为： {}", id);
        // 调用ai分析简历
        try {
            ResumeAnalysisResponse resumeAnalysisResponse = resumeAiService.analyzeResume(id, resumeDTO.resumeText());
        }catch (Exception e){
            log.error("AI分析失败, Id为： {}", id, e);
        }
    }
}
