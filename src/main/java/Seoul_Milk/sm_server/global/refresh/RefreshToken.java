package Seoul_Milk.sm_server.global.refresh;

import Seoul_Milk.sm_server.login.entity.MemberEntity;
import Seoul_Milk.sm_server.login.entity.RefreshEntity;
import Seoul_Milk.sm_server.login.repository.MemberJpaRepository;
import Seoul_Milk.sm_server.login.repository.RefreshJpaRepository;
import java.util.Date;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RefreshToken {
    private final MemberJpaRepository memberJpaRepository;
    private final RefreshJpaRepository refreshJpaRepository;

    public void addRefreshEntity(String employeeId, String refreshToken, Long expiredMs) {

        Date date = new Date(System.currentTimeMillis() + expiredMs);

        MemberEntity memberEntity = memberJpaRepository.findByEmployeeId(employeeId);
        RefreshEntity refreshEntity = RefreshEntity.createRefreshEntity(memberEntity, refreshToken, date);
        refreshJpaRepository.save(refreshEntity);
    }
}
