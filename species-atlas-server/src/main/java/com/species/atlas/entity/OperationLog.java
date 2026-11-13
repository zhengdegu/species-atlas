package com.species.atlas.entity;

import com.species.atlas.enums.OperationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sys_operation_log", indexes = {
        @Index(name = "idx_ol_user_id", columnList = "user_id"),
        @Index(name = "idx_ol_target_table", columnList = "target_table"),
        @Index(name = "idx_ol_create_time", columnList = "create_time")
})
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 操作用户ID
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * 操作用户名
     */
    @Column(name = "username", length = 50)
    private String username;

    /**
     * 操作类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", length = 20)
    private OperationType operationType;

    /**
     * 目标表
     */
    @Column(name = "target_table", length = 50)
    private String targetTable;

    /**
     * 目标ID
     */
    @Column(name = "target_id")
    private Long targetId;

    /**
     * 操作描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 原数据（JSON）
     */
    @Column(name = "old_data", columnDefinition = "JSON")
    private String oldData;

    /**
     * 新数据（JSON）
     */
    @Column(name = "new_data", columnDefinition = "JSON")
    private String newData;

    /**
     * 操作IP
     */
    @Column(name = "ip", length = 50)
    private String ip;

    /**
     * 用户代理
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;
}
