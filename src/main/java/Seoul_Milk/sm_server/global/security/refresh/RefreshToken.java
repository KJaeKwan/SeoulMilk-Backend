package Seoul_Milk.sm_server.global.security.refresh;

import Seoul_Milk.sm_server.global.infrastructure.redis.RedisUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RefreshToken {
    private final RedisUtils redisUtils;

    public void addRefreshEntity(String employeeId, String refreshToken, Long expiredMs) {
        redisUtils.setData(employeeId, refreshToken, expiredMs);
    }
}
