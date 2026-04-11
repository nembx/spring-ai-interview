package org.nembx.app.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Lian
 */

@Getter
@RequiredArgsConstructor
public enum ContentType {
    PDF("application/pdf"),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    MARKDOWN("text/markdown"),
    TXT("text/plain");

    private final String value;
}
