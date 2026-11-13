package com.species.atlas.exception;

import lombok.Getter;

/**
 * 错误码枚举
 */
@Getter
public enum ErrorCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 客户端错误 4xx
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    CONFLICT(409, "资源冲突"),
    VALIDATION_ERROR(422, "参数验证失败"),
    VALIDATION_FAILED(422, "数据验证失败"),

    // 服务端错误 5xx
    INTERNAL_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),

    // 业务错误 1xxx
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户名已存在"),
    INVALID_PASSWORD(1003, "密码错误"),
    USER_DISABLED(1004, "用户已被禁用"),
    EMAIL_ALREADY_EXISTS(1005, "邮箱已被注册"),

    // 认证错误 2xxx
    TOKEN_EXPIRED(2001, "Token 已过期"),
    TOKEN_INVALID(2002, "Token 无效"),
    TOKEN_MISSING(2003, "Token 缺失"),
    REFRESH_TOKEN_INVALID(2004, "Refresh Token 无效"),

    // 物种数据错误 3xxx
    SPECIES_NOT_FOUND(3001, "物种数据不存在"),
    SPECIES_CODE_EXISTS(3002, "物种编码已存在"),
    INVALID_SPECIES_TYPE(3003, "无效的物种类型"),

    // 文件错误 4xxx
    FILE_UPLOAD_ERROR(4001, "文件上传失败"),
    FILE_TYPE_NOT_ALLOWED(4002, "文件类型不允许"),
    FILE_SIZE_EXCEEDED(4003, "文件大小超出限制"),
    FILE_NOT_FOUND(4004, "文件不存在");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
