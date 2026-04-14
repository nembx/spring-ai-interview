package org.nembx.app.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Lian
 */
@Getter
@RequiredArgsConstructor
public enum TextExtensions {
    MD("md"),
    TXT("txt");

    private final String value;
}
