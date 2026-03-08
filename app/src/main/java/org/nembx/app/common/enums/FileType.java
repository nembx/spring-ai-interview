package org.nembx.app.common.enums;


import lombok.Getter;

/**
 * @author Lian
 */
@Getter
public enum FileType {
    KNOWLEDGE("knowledge"),
    RESUME("resume");

    private final String value;

    FileType(String value) {
        this.value = value;
    }
}
