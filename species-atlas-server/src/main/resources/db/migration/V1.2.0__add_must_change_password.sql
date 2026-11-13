-- 添加首次登录强制修改密码字段
ALTER TABLE sys_user ADD COLUMN must_change_password TINYINT(1) DEFAULT 0 COMMENT '是否需要修改密码：0-否，1-是' AFTER role;

-- 设置初始管理员需要修改密码
UPDATE sys_user SET must_change_password = 1 WHERE username = 'admin';
