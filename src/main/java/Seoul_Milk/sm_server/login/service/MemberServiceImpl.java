package Seoul_Milk.sm_server.login.service;

import Seoul_Milk.sm_server.global.exception.CustomException;
import Seoul_Milk.sm_server.global.exception.ErrorCode;
import Seoul_Milk.sm_server.login.dto.request.UpdatePwDTO;
import Seoul_Milk.sm_server.login.entity.MemberEntity;
import Seoul_Milk.sm_server.login.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * 회원 정보
     */
    @Override
    public MemberEntity getMember(String employeeId) {
        return memberRepository.getByEmployeeId(employeeId);
    }

    /**
     * 비밀번호 변경
     */
    @Override
    @Transactional
    public void updatePw(Long memberId, UpdatePwDTO request) {
        MemberEntity member = memberRepository.getById(memberId);

        // 입력 비밀번호 2개 일치 여부 검증
        if (!request.password1().equals(request.password2())) {
            throw new CustomException(ErrorCode.PASSWORDS_NOT_MATCH);
        }

        // 동일한 비밀번호로 변경 불가
        if (passwordEncoder.matches(request.password1(), member.getPassword())) {
            throw new CustomException(ErrorCode.USER_SAME_PASSWORD);
        }

        String newPassword = passwordEncoder.encode(request.password1());
        member.updatePassword(newPassword);
    }
}
