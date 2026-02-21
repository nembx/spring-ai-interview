package org.nembx.app.module.resume.enity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.nembx.app.common.status.TaskStatus;

import java.time.LocalDateTime;

/**
 * @author Lian
 */
@Table(name = "resume")
@Data
@Entity
@Accessors(chain = true)
public class Resume {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileHash;

    private String fileName;

    private Long fileSize;

    private String contentType;

    private String storageKey;

    private String storageUrl;

    private String content;

    private LocalDateTime uploadTime;

    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.PENDING;

    private Boolean isDeleted;

    @PostPersist
    protected void onCreate() {
        uploadTime = LocalDateTime.now();
        isDeleted = false;
    }
}
