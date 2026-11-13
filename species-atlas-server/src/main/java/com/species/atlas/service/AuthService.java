package com.species.atlas.service;

import com.species.atlas.dto.request.ChangePasswordRequest;
import com.species.atlas.dto.request.LoginRequest;
import com.species.atlas.dto.request.RefreshTokenRequest;
import com.species.atlas.dto.request.RegisterRequest;
import com.species.atlas.dto.response.LoginResponse;
import com.species.atlas.dto.response.UserResponse;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @param ip 登录 IP
     * @return 登录响应（包含 Token）
     */
    LoginResponse login(LoginRequest request, String ip);

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 用户信息
     */
    UserResponse register(RegisterRequest request);

    /**
     * 刷新 Token
     *
     * @param request 刷新请求
     * @return 新的登录响应
     */
    LoginResponse refreshToken(RefreshTokenRequest request);

    /**
     * 获取当前用户信息
     *
     * @param userId 用户 ID
     * @return 用户信息
     */
    UserResponse getProfile(Long userId);

    /**
     * 修改密码
     *
     * @param userId 用户 ID
     * @param request 修改密码请求
     */
    void changePassword(Long userId, ChangePasswordRequest request);
}
