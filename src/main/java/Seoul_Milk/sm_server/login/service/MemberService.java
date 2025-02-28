package Seoul_Milk.sm_server.login.service;

import Seoul_Milk.sm_server.login.dto.UpdatePwDTO;
import Seoul_Milk.sm_server.login.dto.VerifyPwDTO;
import Seoul_Milk.sm_server.login.entity.MemberEntity;

public interface MemberService {
    MemberEntity getMember(String employeeId);
    void updatePw(Long memberId, UpdatePwDTO request);
    boolean verifyPassword(Long memberId, VerifyPwDTO request);
}
