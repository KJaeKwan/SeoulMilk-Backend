package Seoul_Milk.sm_server.global.infrastructure.redis;

import static Seoul_Milk.sm_server.global.security.token.Token.REFRESH_TOKEN;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RedisUtils {
    private final RedisTemplate<String, Object> redisTemplate;
    private final HashOperations<String, String, Object> hashOperations;

    public RedisUtils(RedisTemplate<String, Object> redisTemplate,
            HashOperations<String, String, Object> hashOperations) {
        this.redisTemplate = redisTemplate;
        this.hashOperations = hashOperations;
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

    // 🔹 간편인증전 진위여부 api 호출 시 응답 값 저장 (Hash 사용)
    public void saveCodefApiResponse(String id, Map<String, Object> data) {
        Map<String, Object> convertedData = data.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().toString()  // 모든 값을 String으로 변환(타입 안정성 위함)
                ));
        hashOperations.putAll(id, convertedData);
        redisTemplate.expire(id, 5, TimeUnit.MINUTES); // TTL 5분 설정(api정책 때문에 간편인증 4분30초 안에 해야함)
    }

    // 🔹 간편인증전 진위여부 api 호출 시 응답 값 조회
    public Map<String, Object> getCodefApiResponse(String id) {
        return hashOperations.entries(id);
    }
}
