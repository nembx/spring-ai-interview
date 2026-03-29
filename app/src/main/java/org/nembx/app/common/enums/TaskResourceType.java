package org.nembx.app.common.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.nembx.app.common.exception.BusinessException;
import org.nembx.app.common.exception.ErrorCode;

import java.util.Arrays;

/**
 * @author Lian
 */
@Getter
@RequiredArgsConstructor
public enum TaskResourceType {
    RESUME("resume"),
    KNOWLEDGE("knowledge");

    private final String value;

    public static TaskResourceType fromValue(String value) {
        return Arrays.stream(values())
                .filter(type -> type.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_ERROR, "不支持的任务资源类型"));
    }
}
