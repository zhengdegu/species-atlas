-- 操作日志表
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '操作用户名',
    operation_type VARCHAR(20) COMMENT '操作类型：CREATE/UPDATE/DELETE/BATCH_DELETE/IMPORT/EXPORT/LOGIN/LOGOUT',
    target_table VARCHAR(50) COMMENT '目标表',
    target_id BIGINT COMMENT '目标ID',
    description VARCHAR(500) COMMENT '操作描述',
    old_data JSON COMMENT '原数据',
    new_data JSON COMMENT '新数据',
    ip VARCHAR(50) COMMENT '操作IP',
    user_agent VARCHAR(500) COMMENT '用户代理',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_ol_user_id (user_id),
    INDEX idx_ol_target_table (target_table),
    INDEX idx_ol_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';
