package com.species.atlas.controller;

import com.species.atlas.annotation.CurrentUser;
import com.species.atlas.dto.request.ChangePasswordRequest;
import com.species.atlas.dto.request.LoginRequest;
import com.species.atlas.dto.request.RefreshTokenRequest;
import com.species.atlas.dto.request.RegisterRequest;
import com.species.atlas.dto.response.ApiResponse;
import com.species.atlas.dto.response.LoginResponse;
import com.species.atlas.dto.response.UserResponse;
import com.species.atlas.security.SecurityUser;
import com.species.atlas.service.AuthService;
import com.species.atlas.util.IpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户登录、注册、Token刷新等接口")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名和密码登录，返回 JWT Token")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        String ip = IpUtil.getClientIp(httpRequest);
        LoginResponse response = authService.login(request, ip);
        return ApiResponse.success("登录成功", response);
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册新用户（需要管理员权限）")
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ApiResponse.success("注册成功", response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新Token", description = "使用 Refresh Token 获取新的 Access Token")
    public ApiResponse<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(request);
        return ApiResponse.success(response);
    }

    @GetMapping("/profile")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public ApiResponse<UserResponse> getProfile(@CurrentUser SecurityUser currentUser) {
        UserResponse response = authService.getProfile(currentUser.getUserId());
        return ApiResponse.success(response);
    }

    @PostMapping("/change-password")
    @Operation(summary = "修改密码", description = "修改当前用户的密码")
    public ApiResponse<Void> changePassword(
            @CurrentUser SecurityUser currentUser,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(currentUser.getUserId(), request);
        return ApiResponse.success("密码修改成功", null);
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录", description = "退出登录（客户端清除 Token 即可）")
    public ApiResponse<Void> logout() {
        // JWT 是无状态的，退出登录只需客户端清除 Token
        // 如需服务端记录，可在此处添加 Token 黑名单逻辑
        return ApiResponse.success("退出成功", null);
    }
}
