package org.nembx.app.module.interview.entity.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SoftDelete;
import org.nembx.app.common.enums.MessageType;

import java.time.LocalDateTime;

/**
 * @author Lian
 */
@Entity
@Table(name = "interview_message")
@Data
@Accessors(chain = true)
@SoftDelete(columnName = "is_deleted")
@Schema(description = "面试消息")
public class InterviewMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "面试消息ID")
    private Long id;

    @Schema(description = "面试会话ID")
    private Long sessionId;

    @Enumerated(EnumType.STRING)
    @Schema(description = "消息来源")
    private MessageType type;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "消息是否完成")
    private Boolean completed = false;

    @Schema(description = "创建时间")
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
