package Seoul_Milk.sm_server.login.repository;

import Seoul_Milk.sm_server.login.entity.MemberEntity;
import java.util.Optional;

public interface MemberRepository {
    Optional<MemberEntity> findById(Long id);
    MemberEntity getById(Long id);
    Optional<MemberEntity> findByEmployeeId(String employeeId);
    MemberEntity getByEmployeeId(String employeeId);
    Optional<MemberEntity> findByEmail(String email);
    Boolean existsByEmployeeId(String employeeId);
    void save(MemberEntity memberEntity);
}
