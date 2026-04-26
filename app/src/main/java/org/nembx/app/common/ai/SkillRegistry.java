package org.nembx.app.common.ai;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.entity.dto.SkillMetadata;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Lian
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SkillRegistry {
    private static final String SKILL_PATTERN = "classpath*:skills/*/SKILL.md";
    private static final Pattern DESCRIPTION = Pattern.compile(
            "(?m)^-\\s*\\*\\*Description\\*\\*\\s*[:：]\\s*(.+?)。?\\s*$");
    private static final Pattern TRIGGERS = Pattern.compile(
            "(?m)^-\\s*\\*\\*触发条件\\*\\*\\s*[:：]\\s*(.+?)。?\\s*$");

    private final Map<String, SkillMetadata> skills = new LinkedHashMap<>();

    @PostConstruct
    void load() throws IOException {
        for (Resource resource : new PathMatchingResourcePatternResolver().getResources(SKILL_PATTERN)) {
            String name = extractSkillName(resource);
            String content = resource.getContentAsString(StandardCharsets.UTF_8);
            skills.put(name, new SkillMetadata(name, match(DESCRIPTION, content), parseTriggers(content), content));
        }
        log.info("[SkillRegistry] Loaded skills: {}", skills.isEmpty() ? "无" : skills.keySet());
    }

    private String extractSkillName(Resource resource) throws IOException {
        String url = resource.getURL().toString();
        int end = url.lastIndexOf("/SKILL.md");
        int start = url.lastIndexOf("/", end - 1);
        return url.substring(start + 1, end);
    }

    private String match(Pattern pattern, String content) {
        Matcher m = pattern.matcher(content);
        return m.find() ? m.group(1).trim() : "";
    }

    private List<String> parseTriggers(String content) {
        String raw = match(TRIGGERS, content);
        if (raw.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(raw.split("[；;]"))
                .map(s -> s.replaceAll("。$", "").trim())
                .filter(s -> !s.isEmpty())
                .toList();
    }

    public String renderRegistry() {
        StringBuilder sb = new StringBuilder();
        sb.append("以下是面试系统支持的 ").append(skills.size())
                .append(" 种面试技能包（skill），用于根据候选人简历和岗位 JD 匹配最合适的一种。每个 skill 的 key 是英文短名，description 描述其定位，触发条件给出匹配规则。\n\n");
        for (SkillMetadata skill : skills.values()) {
            sb.append("- **").append(skill.name()).append("**：").append(skill.description()).append("\n");
            sb.append("  - 触发：").append(String.join("；", skill.triggers())).append("\n\n");
        }
        return sb.toString().trim();
    }

    public String getBody(String name) {
        SkillMetadata skill = skills.get(name);
        if (skill == null) {
            return "";
        }
        return skill.body();
    }

    public List<String> listNames() {
        return List.copyOf(skills.keySet());
    }
}
