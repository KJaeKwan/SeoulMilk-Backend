package Seoul_Milk.sm_server.login.service;

import Seoul_Milk.sm_server.login.dto.request.RegisterDTO;
import Seoul_Milk.sm_server.login.dto.request.ResetPwDTO;
import Seoul_Milk.sm_server.login.dto.request.UpdateRoleDTO;
import Seoul_Milk.sm_server.login.dto.request.UpdatePwDTO;
import Seoul_Milk.sm_server.login.dto.response.MemberResponse;
import Seoul_Milk.sm_server.login.dto.VerifyPwDTO;
import Seoul_Milk.sm_server.login.entity.MemberEntity;

public interface MemberService {
    MemberResponse getMember(String employeeId);
    MemberEntity getMemberEntity(String employeeId);
    void resetPw(ResetPwDTO request);
    void updatePw(Long memberId, UpdatePwDTO request);
    MemberResponse updateRole(UpdateRoleDTO request);
    boolean verifyPassword(Long memberId, VerifyPwDTO request);
    MemberResponse register(RegisterDTO request);
    Boolean existsByEmployeeId(String employeeId);
}
