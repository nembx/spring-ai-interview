package org.nembx.app.module.knowledge.enity;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.nembx.app.common.enums.TaskStatus;

import java.time.LocalDateTime;

/**
 * @author Lian
 */

@Entity
@Table(name = "knowledge")
@Data
@Accessors(chain = true)
@Schema(description = "知识库")
public class Knowledge {
    @Id
    @Schema(description = "知识库ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "文件Hash")
    private String fileHash;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "文件分类")
    private String category;

    @Schema(description = "文件大小")
    private Long fileSize;

    @Schema(description = "文件类型")
    private String fileType;

    @Schema(description = "存储Key")
    private String storageKey;

    @Schema(description = "存储URL")
    private String storageUrl;

    @Schema(description = "上传时间")
    private LocalDateTime uploadTime;

    @Schema(description = "任务状态")
    @Enumerated(EnumType.STRING)
    private TaskStatus taskStatus = TaskStatus.PENDING;

    @Schema(description = "是否删除")
    private Boolean isDeleted = false;

    @PrePersist
    protected void onCreate() {
        uploadTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        uploadTime = LocalDateTime.now();
    }
}
