package Seoul_Milk.sm_server.login.service;

import Seoul_Milk.sm_server.login.dto.request.TestUpdateRoleDTO;
import Seoul_Milk.sm_server.login.dto.request.UpdatePwDTO;
import Seoul_Milk.sm_server.login.dto.response.MemberResponse;
import Seoul_Milk.sm_server.login.entity.MemberEntity;

public interface MemberService {
    MemberEntity getMember(String employeeId);
    void updatePw(Long memberId, UpdatePwDTO request);
    MemberResponse testUpdateRole(TestUpdateRoleDTO request);
}
