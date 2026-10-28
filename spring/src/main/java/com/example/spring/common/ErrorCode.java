package com.example.spring.common;

import lombok.Getter;

/**
 * 业务错误码(与接口文档错误码总表一致)
 */
@Getter
public enum ErrorCode {

    PARAM_ERROR(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或 Token 无效/过期"),
    FORBIDDEN(403, "无该资源操作权限"),
    NOT_FOUND(404, "请求的资源不存在"),
    SERVER_ERROR(500, "服务器内部错误"),

    USERNAME_EXISTS(1001, "用户名已存在"),
    LOGIN_FAILED(1002, "用户名或密码错误"),
    ACCOUNT_DISABLED(1003, "账号已被禁用，请联系管理员"),

    TASK_NOT_FOUND(2001, "实训任务不存在或已删除"),
    TASK_CLOSED(2002, "任务已截止，禁止提交作业"),

    FILE_TOO_LARGE(3001, "文件大小超出限制（最大 10MB）"),
    FILE_TYPE_NOT_ALLOWED(3002, "文件类型不允许（仅支持 zip/rar/pdf/doc/docx/md）"),

    SUBMISSION_NOT_FOUND(4001, "提交记录不存在"),
    SCORE_OUT_OF_RANGE(4002, "评分超出范围（0~100）");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
