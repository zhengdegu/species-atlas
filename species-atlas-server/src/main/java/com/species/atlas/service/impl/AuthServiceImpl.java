package com.species.atlas.service.impl;

import com.species.atlas.converter.UserConverter;
import com.species.atlas.dto.request.ChangePasswordRequest;
import com.species.atlas.dto.request.LoginRequest;
import com.species.atlas.dto.request.RefreshTokenRequest;
import com.species.atlas.dto.request.RegisterRequest;
import com.species.atlas.dto.response.LoginResponse;
import com.species.atlas.dto.response.UserResponse;
import com.species.atlas.entity.User;
import com.species.atlas.enums.UserRole;
import com.species.atlas.enums.UserStatus;
import com.species.atlas.exception.BusinessException;
import com.species.atlas.exception.ErrorCode;
import com.species.atlas.exception.NotFoundException;
import com.species.atlas.exception.UnauthorizedException;
import com.species.atlas.repository.UserRepository;
import com.species.atlas.security.JwtTokenProvider;
import com.species.atlas.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserConverter userConverter;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, String ip) {
        // 认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // 获取用户
        User user = userRepository.findByUsernameAndDeletedFalse(request.getUsername())
                .orElseThrow(() -> new NotFoundException("用户", request.getUsername()));

        // 检查状态
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_DISABLED, "用户已被禁用，请联系管理员");
        }

        // 更新登录信息
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        userRepository.save(user);

        // 生成 Token
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());

        log.info("用户 {} 登录成功，IP: {}", user.getUsername(), ip);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationSeconds())
                .mustChangePassword(Boolean.TRUE.equals(user.getMustChangePassword()))
                .user(userConverter.toResponse(user))
                .build();
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        // 检查用户名是否存在
        if (userRepository.existsByUsernameAndDeletedFalse(request.getUsername())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS, "用户名已被使用");
        }

        // 检查邮箱是否存在
        if (request.getEmail() != null && userRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS, "邮箱已被注册");
        }

        // 创建用户
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .email(request.getEmail())
                .phone(request.getPhone())
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();

        user = userRepository.save(user);
        log.info("用户注册成功: {}", user.getUsername());

        return userConverter.toResponse(user);
    }

    @Override
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // 验证 Refresh Token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException(ErrorCode.REFRESH_TOKEN_INVALID, "Refresh Token 无效或已过期");
        }

        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new UnauthorizedException(ErrorCode.REFRESH_TOKEN_INVALID, "无效的 Refresh Token");
        }

        // 获取用户信息
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

        User user = userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new NotFoundException("用户", username));

        // 检查状态
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }

        // 生成新的 Token
        String newAccessToken = jwtTokenProvider.generateAccessToken(userId, username);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId, username);

        log.info("用户 {} 刷新 Token 成功", username);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationSeconds())
                .user(userConverter.toResponse(user))
                .build();
    }

    @Override
    public UserResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .filter(u -> !u.getDeleted())
                .orElseThrow(() -> new NotFoundException("用户", userId));

        return userConverter.toResponse(user);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        // 验证新密码与确认密码一致
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "新密码与确认密码不一致");
        }

        // 获取用户
        User user = userRepository.findById(userId)
                .filter(u -> !u.getDeleted())
                .orElseThrow(() -> new NotFoundException("用户", userId));

        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "旧密码不正确");
        }

        // 检查新密码是否与旧密码相同
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "新密码不能与旧密码相同");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        // 清除强制修改密码标记
        user.setMustChangePassword(false);
        userRepository.save(user);

        log.info("用户 {} 修改密码成功", user.getUsername());
    }
}
