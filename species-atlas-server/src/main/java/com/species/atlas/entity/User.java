package com.species.atlas.entity;

import com.species.atlas.enums.UserRole;
import com.species.atlas.enums.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_user", indexes = {
        @Index(name = "idx_username", columnList = "username", unique = true),
        @Index(name = "idx_email", columnList = "email")
})
public class User extends BaseEntity {

    /**
     * 用户名
     */
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 密码（加密后）
     */
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /**
     * 昵称
     */
    @Column(name = "nickname", length = 50)
    private String nickname;

    /**
     * 邮箱
     */
    @Column(name = "email", length = 100)
    private String email;

    /**
     * 手机号
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * 头像URL
     */
    @Column(name = "avatar", length = 500)
    private String avatar;

    /**
     * 用户状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    /**
     * 用户角色
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20)
    @Builder.Default
    private UserRole role = UserRole.USER;

    /**
     * 是否需要修改密码（首次登录）
     */
    @Column(name = "must_change_password")
    @Builder.Default
    private Boolean mustChangePassword = false;

    /**
     * 最后登录时间
     */
    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    @Column(name = "last_login_ip", length = 50)
    private String lastLoginIp;
}
