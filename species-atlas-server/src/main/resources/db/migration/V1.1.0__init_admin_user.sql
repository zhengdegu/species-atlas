-- 初始化管理员用户
-- 密码: admin123 (BCrypt 加密)
INSERT INTO sys_user (username, password, nickname, email, status, role, create_time)
VALUES (
    'admin',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6KhOq',
    '系统管理员',
    'admin@species-atlas.com',
    'ACTIVE',
    'ADMIN',
    NOW()
);
