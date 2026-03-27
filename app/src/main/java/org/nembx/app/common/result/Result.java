package org.nembx.app.common.result;


import org.nembx.app.common.exception.ErrorCode;

/**
 * @author Lian
 */
public record Result<T>(Integer code, String message, T data) {

    public static <T> Result<T> success() {
        return new Result<>(ErrorCode.SUCCESS.getCode(), "success", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ErrorCode.SUCCESS.getCode(), message, data);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(ErrorCode.INTERNAL_ERROR.getCode(), message, null);
    }

    public static <T> Result<T> error(ErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

}
