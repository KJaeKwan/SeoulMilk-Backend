package Seoul_Milk.sm_server.domain.member.service;

import Seoul_Milk.sm_server.domain.member.dto.request.RegisterDTO;
import Seoul_Milk.sm_server.domain.member.dto.request.ResetPwDTO;
import Seoul_Milk.sm_server.domain.member.dto.request.UpdateRoleDTO;
import Seoul_Milk.sm_server.domain.member.dto.request.UpdatePwDTO;
import Seoul_Milk.sm_server.domain.member.dto.response.MemberResponse;
import Seoul_Milk.sm_server.domain.member.dto.request.VerifyPwDTO;
import Seoul_Milk.sm_server.domain.member.entity.MemberEntity;

public interface MemberService {
    MemberResponse getMember(String employeeId);
    MemberEntity getMemberEntity(String employeeId);
    void resetPw(Long memberId, ResetPwDTO request);
    void updatePw(Long memberId, UpdatePwDTO request);
    MemberResponse updateRole(UpdateRoleDTO request);
    boolean verifyPassword(Long memberId, VerifyPwDTO request);
    MemberResponse register(RegisterDTO request);
    Boolean existsByEmployeeId(String employeeId);
}
