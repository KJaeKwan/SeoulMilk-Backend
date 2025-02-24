package Seoul_Milk.sm_server.login.repository;

import Seoul_Milk.sm_server.login.entity.MemberEntity;

public interface MemberRepository {
    MemberEntity findByEmployeeId(String employeeId);
    Boolean existsByEmployeeId(String employeeId);
    void save(MemberEntity memberEntity);
}
