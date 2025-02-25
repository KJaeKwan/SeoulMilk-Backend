package Seoul_Milk.sm_server.login.repository;

import Seoul_Milk.sm_server.login.entity.MemberEntity;
import java.util.Optional;

public interface MemberRepository {
    Optional<MemberEntity> findByEmployeeId(String employeeId);
    Optional<MemberEntity> findByEmail(String email);
    Boolean existsByEmployeeId(String employeeId);
    void save(MemberEntity memberEntity);
}
