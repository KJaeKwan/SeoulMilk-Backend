package Seoul_Milk.sm_server.domain.member.service;

import Seoul_Milk.sm_server.global.exception.CustomException;
import Seoul_Milk.sm_server.global.exception.ErrorCode;
import Seoul_Milk.sm_server.domain.member.enums.Role;
import Seoul_Milk.sm_server.domain.member.dto.request.VerifyPwDTO;
import Seoul_Milk.sm_server.domain.member.dto.request.RegisterDTO;
import Seoul_Milk.sm_server.domain.member.dto.request.ResetPwDTO;
import Seoul_Milk.sm_server.domain.member.dto.request.UpdatePwDTO;
import Seoul_Milk.sm_server.domain.member.dto.request.UpdateRoleDTO;
import Seoul_Milk.sm_server.domain.member.dto.response.MemberResponse;
import Seoul_Milk.sm_server.domain.member.entity.MemberEntity;
import Seoul_Milk.sm_server.domain.member.repository.MemberRepository;
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
    public MemberResponse getMember(String employeeId) {
        MemberEntity member = memberRepository.getByEmployeeId(employeeId);
        return MemberResponse.from(member);
    }

    /**
     * CurrentMember 어노테이션을 위한 메서드
     */
    public MemberEntity getMemberEntity(String employeeId) {
        return memberRepository.getByEmployeeId(employeeId);
    }

    @Override
    @Transactional
    public void resetPw(Long memberId, ResetPwDTO request) {
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

    /**
     * 비밀번호 변경
     */
    @Override
    @Transactional
    public void updatePw(Long memberId, UpdatePwDTO request) {
        MemberEntity member = memberRepository.getById(memberId);

        if (!passwordEncoder.matches(request.currentPassword(), member.getPassword())) {
            throw new CustomException(ErrorCode.USER_WRONG_PASSWORD);
        }

        // 입력 비밀번호 2개 일치 여부 검증
        if (!request.newPassword1().equals(request.newPassword2())) {
            throw new CustomException(ErrorCode.PASSWORDS_NOT_MATCH);
        }

        // 동일한 비밀번호로 변경 불가
        if (passwordEncoder.matches(request.newPassword1(), member.getPassword())) {
            throw new CustomException(ErrorCode.USER_SAME_PASSWORD);
        }

        String newPassword = passwordEncoder.encode(request.newPassword1());
        member.updatePassword(newPassword);
    }

    /**
     * 비밀번호 확인
     */
    @Override
    public boolean verifyPassword(Long memberId, VerifyPwDTO request) {
        MemberEntity member = memberRepository.getById(memberId);
        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new CustomException(ErrorCode.USER_WRONG_PASSWORD);
        }
        return true;
    }

    /**
     * 사용자 권한 변경
     */
    @Override
    @Transactional
    public MemberResponse updateRole(UpdateRoleDTO request) {
        MemberEntity member = memberRepository.getById(request.memberId());

        try {
            Role role = Role.valueOf(request.role());
            member.updateRole(role);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_ROLE);
        }

        return MemberResponse.from(member);
    }

    /**
     * 사원 등록
     */
    @Override
    @Transactional
    public MemberResponse register(RegisterDTO request) {
        if (memberRepository.existsByEmployeeId(request.employeeId())) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXIST);
        }

        // 비밀번호 초기 설정
        String encodedPassword = passwordEncoder.encode("0000");

        MemberEntity member = MemberEntity.createVerifiedMember(request.employeeId(), request.name(), encodedPassword, Role.valueOf(request.role()));
        MemberEntity savedMember = memberRepository.save(member);

        return MemberResponse.from(savedMember);
    }

    /**
     * 사원 존재 여부 검증
     */
    public Boolean existsByEmployeeId(String employeeId) {
        return memberRepository.existsByEmployeeId(employeeId);
    }
}
