package com.species.atlas.exception;

/**
 * 请求参数异常
 */
public class BadRequestException extends BusinessException {

    public BadRequestException(String message) {
        super(ErrorCode.BAD_REQUEST, message);
    }
}
