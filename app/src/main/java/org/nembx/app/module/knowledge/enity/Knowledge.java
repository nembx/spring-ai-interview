package org.nembx.app.module.knowledge.enity;


import jakarta.persistence.*;
import lombok.Data;
import org.nembx.app.common.status.TaskStatus;

import java.time.LocalDateTime;

/**
 * @author Lian
 */

@Entity
@Table(name = "knowledge")
@Data
public class Knowledge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileHash;

    private String fileName;

    private String category;

    private String originalFileName;

    private Long fileSize;

    private String fileType;

    private String storageKey;

    private String storageUrl;

    private LocalDateTime uploadTime;

    @Enumerated(EnumType.STRING)
    private TaskStatus taskStatus = TaskStatus.PENDING;

    private Boolean isDeleted = false;

    @PrePersist
    protected void onCreate() {
        this.uploadTime = LocalDateTime.now();
    }
}
