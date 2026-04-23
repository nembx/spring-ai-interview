package org.nembx.app.common.ai;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Lian
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiPromptManager {
    private static final String PROMPT_PREFIX = "classpath:prompt/";
    private static final String PROMPT_SUFFIX = ".st";

    private final ResourceLoader resourceLoader;
    private final ConcurrentHashMap<String, PromptTemplate> cache = new ConcurrentHashMap<>();

    public PromptTemplate getTemplate(String name) {
        return cache.computeIfAbsent(PROMPT_PREFIX + name + PROMPT_SUFFIX, this::loadTemplate);
    }

    /**
     * 渲染无参数模板
     */
    public String render(String name) {
        return getTemplate(name).render();
    }

    /**
     * 渲染带参数模板
     */
    public String render(String name, Map<String, Object> variables) {
        return getTemplate(name).render(variables);
    }

    private PromptTemplate loadTemplate(String path) {
        try {
            String content = resourceLoader.getResource(path)
                    .getContentAsString(StandardCharsets.UTF_8);
            log.info("[Prompt模板加载] {}", path);
            return new PromptTemplate(content);
        } catch (IOException e) {
            throw new IllegalStateException("加载Prompt模板失败: " + path, e);
        }
    }
}
