package org.nembx.app.common.service;

import lombok.RequiredArgsConstructor;
import org.nembx.app.common.entity.dto.LimitResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Redis 令牌桶限流服务
 *
 * @author Lian
 */
@Service
@RequiredArgsConstructor
public class RateLimitService {
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 令牌桶算法限流
     *
     * @param key      限流key（如方法名）
     * @param maxCount 桶容量
     * @param rate     每秒填充速率
     * @return true: 通过限流, false: 未通过限流
     */
    public LimitResponse limit(String key, int maxCount, double rate) {
        Long now = System.currentTimeMillis();

        String script = """
                local key = KEYS[1]
                local maxCount = tonumber(ARGV[1])
                local rate = tonumber(ARGV[2])
                local now = tonumber(ARGV[3])
                
                local lastTime = redis.call('GET', key .. ':time') or now
                local tokens = redis.call('GET', key) or maxCount
                
                -- 计算新增令牌数
                local elapsed = math.max(0, now - lastTime)
                local newTokens = math.min(maxCount, tokens + elapsed * rate / 1000)
                
                if newTokens >= 1 then
                    redis.call('SETEX', key, 60, newTokens - 1)
                    redis.call('SETEX', key .. ':time', 60, now)
                    return {1, newTokens - 1}
                else
                    redis.call('SETEX', key, 60, newTokens)
                    redis.call('SETEX', key .. ':time', lastTime)
                    return {0, newTokens}
                end
                """;

        List<?> result = stringRedisTemplate.execute(
                RedisScript.of(script, List.class),
                Collections.singletonList(key),
                String.valueOf(maxCount),
                String.valueOf(rate),
                String.valueOf(now)
        );

        if (result != null && result.size() == 2) {
            boolean allowed = (Long) result.get(0) == 1L;
            long remainingTokens = ((Long) result.get(1)).intValue();
            return new LimitResponse(allowed, remainingTokens);
        }
        return new LimitResponse(false, 0);
    }
}