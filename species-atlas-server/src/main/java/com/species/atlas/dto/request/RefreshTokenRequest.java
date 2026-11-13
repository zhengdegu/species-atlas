package com.species.atlas.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新 Token 请求 DTO
 */
@Data
@Schema(description = "刷新Token请求")
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh Token 不能为空")
    @Schema(description = "Refresh Token")
    private String refreshToken;
}
