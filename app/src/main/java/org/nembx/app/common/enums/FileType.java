package org.nembx.app.common.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Lian
 */
@Getter
@RequiredArgsConstructor
public enum FileType {
    KNOWLEDGE("knowledge"),
    RESUME("resume");

    private final String value;
}
