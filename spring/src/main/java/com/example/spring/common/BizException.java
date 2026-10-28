package com.example.spring.common;

import lombok.Getter;

/**
 * 业务异常,抛出后由全局异常处理器转为统一 Result
 */
@Getter
public class BizException extends RuntimeException {

    private final Integer code;

    public BizException(ErrorCode ec) {
        super(ec.getMessage());
        this.code = ec.getCode();
    }

    public BizException(ErrorCode ec, String message) {
        super(message);
        this.code = ec.getCode();
    }
}
