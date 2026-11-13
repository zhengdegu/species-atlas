package com.species.atlas.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginResponse {

    @Schema(description = "Access Token")
    private String accessToken;

    @Schema(description = "Refresh Token")
    private String refreshToken;

    @Schema(description = "Token 过期时间（秒）", example = "7200")
    private Long expiresIn;

    @Schema(description = "是否需要修改密码", example = "false")
    private Boolean mustChangePassword;

    @Schema(description = "用户信息")
    private UserResponse user;
}
