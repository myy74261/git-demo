package com.writingcoach.web.api;

/**
 * 统一的错误响应结构。
 *
 * @param code 机器可读的错误码
 * @param message 面向用户的说明
 * @param currentRevision 版本冲突时的服务端版本号
 */
public record ApiError(String code, String message, Long currentRevision) {

    public static ApiError of(String code, String message) {
        return new ApiError(code, message, null);
    }
}
