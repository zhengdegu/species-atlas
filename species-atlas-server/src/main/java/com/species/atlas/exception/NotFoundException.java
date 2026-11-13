package com.species.atlas.exception;

/**
 * 资源不存在异常
 */
public class NotFoundException extends BusinessException {

    public NotFoundException(String message) {
        super(ErrorCode.NOT_FOUND, message);
    }

    public NotFoundException(String resource, Object id) {
        super(ErrorCode.NOT_FOUND, String.format("%s 不存在: %s", resource, id));
    }
}
