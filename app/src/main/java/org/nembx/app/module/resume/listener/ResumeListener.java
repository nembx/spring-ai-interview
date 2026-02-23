package org.nembx.app.module.resume.listener;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.module.resume.enity.record.ResumeRecord;
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
    public void onApplicationEvent(ResumeRecord resumeRecord) {
        Long id = resumeRecord.resumeId();
        log.info("收到简历, Id为： {}", id);
        // 调用ai分析简历
        try {
            resumeAiService.analyzeResume(id, resumeRecord.content());
        }catch (Exception e){
            log.error("AI分析失败, Id为： {}", id, e);
        }
    }
}
