package org.nembx.app.common.result;


import lombok.Getter;
import org.nembx.app.common.exception.ErrorCode;

/**
 * @author Lian
 */
@Getter
public class Result<T> {
    private final Integer code;
    private final String message;
    private final T data;

    public Result(Integer code, String message, T data){
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success(){
        return new Result<>(ErrorCode.SUCCESS.getCode(), "success", null);
    }

    public static <T> Result<T> success(String message, T data){
        return new Result<>(ErrorCode.SUCCESS.getCode(), message, data);
    }

    public static <T> Result<T> error(Integer code, String message){
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> error(String message){
        return new Result<>(ErrorCode.INTERNAL_ERROR.getCode(), message, null);
    }

    public static <T> Result<T> error(ErrorCode errorCode){
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

}
