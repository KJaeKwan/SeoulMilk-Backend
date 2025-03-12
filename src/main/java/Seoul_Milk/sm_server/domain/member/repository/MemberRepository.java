package Seoul_Milk.sm_server.domain.member.repository;

import Seoul_Milk.sm_server.domain.member.entity.MemberEntity;
import java.util.Optional;

public interface MemberRepository {
    Optional<MemberEntity> findById(Long id);
    MemberEntity getById(Long id);
    Optional<MemberEntity> findByEmployeeId(String employeeId);
    MemberEntity getByEmployeeId(String employeeId);
    Optional<MemberEntity> findByEmail(String email);
    Boolean existsByEmployeeId(String employeeId);
    MemberEntity save(MemberEntity memberEntity);
}
