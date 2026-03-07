package org.nembx.app.module.knowledge.enity;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.nembx.app.common.enums.SessionStatus;

import java.time.LocalDateTime;

/**
 * @author Lian
 */

@Entity
@Table(name = "rag_session")
@Data
@Accessors(chain = true)
@Schema(description = "rag会话")
public class RagSession {
    @Id
    @Schema(description = "rag会话ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "rag会话标题")
    @Column(nullable = false)
    private String title;

    @Schema(description = "rag会话状态")
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SessionStatus status = SessionStatus.ACTIVE;

    @Schema(description = "关联的知识库ID")
    private String knowledgeIds;

    @Schema(description = "创建时间")
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Schema(description = "最后一次消息更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "rag会话消息数量")
    private Integer messageCount = 0;


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
