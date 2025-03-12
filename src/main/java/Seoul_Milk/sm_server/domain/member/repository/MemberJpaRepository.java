package Seoul_Milk.sm_server.domain.member.repository;

import Seoul_Milk.sm_server.domain.member.entity.MemberEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberJpaRepository extends JpaRepository<MemberEntity, Long> {
    Optional<MemberEntity> findByEmployeeId(String employeeId);
    Boolean existsByEmployeeId(String employeeId);
    Optional<MemberEntity> findByEmail(String email);
}
