package org.nembx.app.common.entity.dto;

import java.util.List;

/**
 * @author Lian
 */
public record SkillMetadata(
        String name,
        String description,
        List<String> triggers,
        String body
) {
}
