package com.example.spring.common;

import lombok.Data;

/**
 * 统一返回结构
 */
@Data
public class Result<T> {

    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage("success");
        r.setData(data);
        return r;
    }

    public static <T> Result<T> ok(String message, T data) {
        Result<T> r = ok(data);
        r.setMessage(message);
        return r;
    }

    public static <T> Result<T> fail(ErrorCode ec) {
        Result<T> r = new Result<>();
        r.setCode(ec.getCode());
        r.setMessage(ec.getMessage());
        return r;
    }

    public static <T> Result<T> fail(Integer code, String message) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }
}
