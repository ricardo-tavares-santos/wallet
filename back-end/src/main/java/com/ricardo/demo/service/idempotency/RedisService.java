package com.ricardo.demo.service.idempotency;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * redis Tools
 */
@Component
public class RedisService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * Write cache
     *
     * @param key
     * @param value
     * @return
     */
    public boolean set(final String key, Object value) {
        boolean result = false;
        try {
            ValueOperations operations = redisTemplate.opsForValue();
            operations.set(key, value);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Write cache set aging time
     *
     * @param key
     * @param value
     * @return
     */
    public boolean setEx(final String key, Object value, Long expireTime) {
        boolean result = false;
        try {
            ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
            operations.set(key, value);
            redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Determine whether there is a corresponding value in the cache
     *
     * @param key
     * @return
     */
    public boolean exists(final String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * Read cache
     * @param key
     * @return
     */
    public Object get(final String key) {
        Object o = null;
        ValueOperations valueOperations = redisTemplate.opsForValue();
        return valueOperations.get(key);
    }

    /**
     * Delete the corresponding value
     * @param key
     */
    public Boolean remove(final String key) {
        if(exists(key)){
            return redisTemplate.delete(key);
        }
        return false;
    }
}
