package org.nembx.app.module.resume.entity;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SoftDelete;
import org.nembx.app.common.enums.TaskStatus;

import java.time.LocalDateTime;

/**
 * @author Lian
 */
@Table(name = "resume")
@Data
@Entity
@Accessors(chain = true)
@SoftDelete(columnName = "is_deleted")
@Schema(description = "简历")
public class Resume {
    @Id
    @Schema(description = "简历ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "文件Hash")
    private String fileHash;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "文件大小")
    private Long fileSize;

    @Schema(description = "文件类型")
    private String contentType;

    @Schema(description = "存储Key")
    private String storageKey;

    @Schema(description = "存储URL")
    private String storageUrl;

    @Schema(description = "文件内容")
    @Column(columnDefinition = "TEXT")
    private String content;

    @Schema(description = "上传时间")
    private LocalDateTime uploadTime;

    @Schema(description = "任务状态")
    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.PENDING;

    @PrePersist
    protected void onCreate() {
        uploadTime = LocalDateTime.now();
    }
}
