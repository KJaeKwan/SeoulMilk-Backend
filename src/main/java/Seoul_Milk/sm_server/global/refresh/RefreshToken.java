package Seoul_Milk.sm_server.global.refresh;

import Seoul_Milk.sm_server.global.redis.RedisUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RefreshToken {
    private final RedisUtils redisUtils;

    public void addRefreshEntity(String employeeId, String refreshToken, Long expiredMs) {
        redisUtils.setData(employeeId, refreshToken, expiredMs);
    }
}
