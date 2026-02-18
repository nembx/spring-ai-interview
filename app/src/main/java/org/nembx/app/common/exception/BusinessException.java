package org.nembx.app.common.exception;


import lombok.Getter;

/**
 * @author Lian
 */
@Getter
public class BusinessException extends RuntimeException{
    private final Integer code;
    private final String message;

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

}
