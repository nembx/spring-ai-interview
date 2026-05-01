package org.nembx.app.common.entity.dto;

/**
 * @author Lian
 */
public record LimitResponse(
        boolean lock,
        long remaining
) {
};
