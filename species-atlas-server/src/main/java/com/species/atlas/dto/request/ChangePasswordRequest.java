package com.species.atlas.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修改密码请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "修改密码请求")
public class ChangePasswordRequest {

    @NotBlank(message = "旧密码不能为空")
    @Schema(description = "旧密码", required = true)
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 100, message = "新密码长度必须在6-100之间")
    @Schema(description = "新密码", required = true)
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认新密码", required = true)
    private String confirmPassword;
}
