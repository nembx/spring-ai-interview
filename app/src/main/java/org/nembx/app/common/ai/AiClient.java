package org.nembx.app.common.ai;

import lombok.extern.slf4j.Slf4j;
import org.nembx.app.common.exception.BusinessException;
import org.nembx.app.common.exception.ErrorCode;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.converter.StructuredOutputConverter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;


/**
 * @author Lian
 */
@Service
@Slf4j
public class AiClient {
    private final ChatClient chatClient;

    public AiClient(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }


    /**
     * 同步文本调用
     */
    public String call(String systemPrompt, String userPrompt) {
        long start = System.currentTimeMillis();
        try {
            String result = prompt(systemPrompt, userPrompt)
                    .call()
                    .content();
            log.info("[AI调用完成] 耗时: {}ms", System.currentTimeMillis() - start);
            return result;
        } catch (Exception e) {
            log.error("[AI调用失败] 耗时: {}ms, 错误: {}", System.currentTimeMillis() - start, e.getMessage());
            throw new BusinessException(ErrorCode.AI_CALL_ERROR, "AI调用失败: " + e.getMessage());
        }
    }

    /**
     * 同步结构化输出调用
     */
    public <T> T call(String systemPrompt, String userPrompt, StructuredOutputConverter<T> converter) {
        long start = System.currentTimeMillis();
        try {
            T result = prompt(systemPrompt, userPrompt)
                    .call()
                    .entity(converter);
            log.info("[AI结构化调用完成] 耗时: {}ms", System.currentTimeMillis() - start);
            return result;
        } catch (Exception e) {
            log.error("[AI结构化调用失败] 耗时: {}ms, 错误: {}", System.currentTimeMillis() - start, e.getMessage());
            throw new BusinessException(ErrorCode.AI_CALL_ERROR, "AI调用失败: " + e.getMessage());
        }
    }

    /**
     * 流式文本调用
     */
    public Flux<String> stream(String systemPrompt, String userPrompt) {
        long start = System.currentTimeMillis();
        return prompt(systemPrompt, userPrompt)
                .stream()
                .content()
                .doOnComplete(() ->
                        log.info("[AI流式调用完成] 耗时: {}ms", System.currentTimeMillis() - start))
                .doOnError(e ->
                        log.error("[AI流式调用失败] 耗时: {}ms, 错误: {}", System.currentTimeMillis() - start, e.getMessage()));
    }

    /**
     * ai client配置拼接
     */
    private ChatClient.ChatClientRequestSpec prompt(String systemPrompt, String userPrompt) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt);
    }
}
