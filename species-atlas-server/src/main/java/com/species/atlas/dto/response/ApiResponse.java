package com.species.atlas.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一 API 响应包装
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "API响应")
public class ApiResponse<T> {

    @Schema(description = "响应码", example = "200")
    private Integer code;

    @Schema(description = "响应消息", example = "success")
    private String msg;

    @Schema(description = "响应数据")
    private T data;

    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder()
                .code(200)
                .msg("success")
                .build();
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .msg("success")
                .data(data)
                .build();
    }

    /**
     * 成功响应（带消息和数据）
     */
    public static <T> ApiResponse<T> success(String msg, T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .msg(msg)
                .data(data)
                .build();
    }

    /**
     * 失败响应
     */
    public static <T> ApiResponse<T> error(Integer code, String msg) {
        return ApiResponse.<T>builder()
                .code(code)
                .msg(msg)
                .build();
    }

    /**
     * 失败响应（默认 500 错误码）
     */
    public static <T> ApiResponse<T> error(String msg) {
        return error(500, msg);
    }
}
