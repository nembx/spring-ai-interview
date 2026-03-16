package org.nembx.app.common.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Lian
 */
@Slf4j
public class JsonUtils {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonUtils() {
    }


    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("JSON 序列化失败, 对象类型: {}", obj.getClass().getName(), e);
            // 抛出运行时异常，避免吃掉异常导致隐患
            throw new RuntimeException("JSON 序列化失败", e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("JSON 反序列化失败, JSON长度: {}, 目标类: {}", json.length(), clazz.getName(), e);
            throw new RuntimeException("JSON 反序列化失败", e);
        }
    }

    /**
     * 将 JSON 字符串转换为复杂泛型对象 (例如 List<MyObj>, Map<String, MyObj> 等)
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.error("JSON 复杂泛型反序列化失败, JSON长度: {}", json.length(), e);
            throw new RuntimeException("JSON 反序列化失败", e);
        }
    }
}
