package Seoul_Milk.sm_server.global.redis;

import static Seoul_Milk.sm_server.global.token.Token.REFRESH_TOKEN;

import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisUtils {
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisUtils(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setData(String key, String value,Long expiredTime){
        redisTemplate.opsForValue().set(REFRESH_TOKEN.category() + key, value, expiredTime, TimeUnit.MILLISECONDS);
    }

    public String getData(String key){
        return (String) redisTemplate.opsForValue().get(key);
    }

    public void deleteData(String key){
        key = makeRefreshKey(key);
        redisTemplate.delete(key);
    }
    public boolean existsValue(String key, String value) {
        key = makeRefreshKey(key);
        String storedToken = getData(key);
        return storedToken != null && storedToken.equals(value);
    }

    public String makeRefreshKey(String key){
        return REFRESH_TOKEN.category() + key;
    }
}
