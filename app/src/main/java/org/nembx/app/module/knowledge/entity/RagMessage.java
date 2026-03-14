package org.nembx.app.module.knowledge.entity;


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
@Table(name = "rag_message")
@Entity
@Data
@Accessors(chain = true)
@SoftDelete(columnName = "is_deleted")
@Schema(description = "RAG会话消息")
public class RagMessage {
    @Id
    @Schema(description = "消息ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "关联的会话ID")
    private Long sessionId;


    @Schema(description = "消息类型")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageType type;

    @Schema(description = "消息内容")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Schema(description = "创建时间")
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "消息是否完成")
    private Boolean completed = false;

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
