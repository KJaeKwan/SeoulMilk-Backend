package Seoul_Milk.sm_server.global.redis;

import static Seoul_Milk.sm_server.global.token.Token.REFRESH_TOKEN;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RedisUtils {
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisUtils(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Refresh Token 전용
    public void setData(String key, String value,Long expiredTime){
        key = makeRefreshKey(key);
        redisTemplate.opsForValue().set(key, value, expiredTime, TimeUnit.MILLISECONDS);
    }

    public void setValues(String key, String data, Duration duration) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        values.set(key, data, duration);
    }

    public String getData(String key){
        return (String) redisTemplate.opsForValue().get(key);
    }

    @Transactional(readOnly = true)
    public String getValues(String key) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        return (String) values.get(key);
    }

    public void deleteData(String key){
        key = makeRefreshKey(key);
        redisTemplate.delete(key);
    }

    public void deleteValues(String key) {
        redisTemplate.delete(key);
    }

    public boolean existsValue(String key, String value) {
        key = makeRefreshKey(key);
        String storedToken = getData(key);
        return storedToken != null && storedToken.equals(value);
    }

    public String makeRefreshKey(String key){
        return REFRESH_TOKEN.category() + ":" + key;
    }
}
