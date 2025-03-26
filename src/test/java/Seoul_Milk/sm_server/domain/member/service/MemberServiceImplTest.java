package Seoul_Milk.sm_server.domain.member.service;

import Seoul_Milk.sm_server.domain.member.dto.request.*;
import Seoul_Milk.sm_server.domain.member.dto.response.MemberResponse;
import Seoul_Milk.sm_server.domain.member.entity.MemberEntity;
import Seoul_Milk.sm_server.domain.member.enums.Role;
import Seoul_Milk.sm_server.global.common.exception.CustomException;
import Seoul_Milk.sm_server.global.common.exception.ErrorCode;
import Seoul_Milk.sm_server.mock.container.TestContainer;
import Seoul_Milk.sm_server.util.SecurityTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class MemberServiceImplTest {

    private TestContainer testContainer;
    private MemberEntity testMember;

    @BeforeEach
    void setUp() {
        testContainer = new TestContainer();

        testMember = MemberEntity.createVerifiedMember(
                "11111111",
                "김재관",
                new BCryptPasswordEncoder().encode("1234"),
                Role.ROLE_ADMIN
        );

        testMember = testContainer.memberRepository.save(this.testMember);
        SecurityTestUtil.setAuthentication(this.testMember);
    }

    @Test
    @DisplayName("회원 정보 조회 성공")
    void getMemberSuccessfully() {
        // Given
        // When
        MemberResponse response = testContainer.memberService.getMember("11111111");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.employeeId()).isEqualTo("11111111");
        assertThat(response.name()).isEqualTo("김재관");
        assertThat(response.role()).isEqualTo(Role.ROLE_ADMIN.name());
    }

    @Test
    @DisplayName("회원 정보 조회 실패 - 존재하지 않는 사용자")
    void getMemberFailedForNonExistentUser() {
        // When
        // Then
        assertThatThrownBy(() -> testContainer.memberService.getMember("99999999"))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_EXIST.getMessage());
    }

    @Test
    @DisplayName("비밀번호 초기화 성공")
    void resetPasswordSuccessfully() {
        // Given
        ResetPwDTO request = ResetPwDTO.builder()
                .employeeId("11111111")
                .password1("9999")
                .password2("9999")
                .build();

        // When
        testContainer.memberService.resetPw(request);

        // Then
        MemberEntity updatedMember = testContainer.memberRepository.getByEmployeeId("11111111");
        assertThat(new BCryptPasswordEncoder().matches("9999", updatedMember.getPassword())).isTrue();
    }

    @Test
    @DisplayName("비밀번호 초기화 실패 - 비밀번호 확인 불일치")
    void resetPasswordFailedDueToMismatchedPassword() {
        // Given
        ResetPwDTO request = ResetPwDTO.builder()
                .employeeId("11111111")
                .password1("9999")
                .password2("8888")
                .build();

        // When
        // Then
        assertThatThrownBy(() -> testContainer.memberService.resetPw(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.PASSWORDS_NOT_MATCH.getMessage());
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void updatePasswordSuccessfully() {
        // Given
        UpdatePwDTO request = UpdatePwDTO.builder()
                .currentPassword("1234")
                .newPassword1("9999")
                .newPassword2("9999")
                .build();

        // When
        testContainer.memberService.updatePw(testMember.getId(), request);

        // Then
        MemberEntity updatedMember = testContainer.memberRepository.getById(testMember.getId());
        assertThat(new BCryptPasswordEncoder().matches("9999", updatedMember.getPassword())).isTrue();
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치")
    void updatePasswordFailedDueToWrongCurrentPassword() {
        // Given
        UpdatePwDTO request = UpdatePwDTO.builder()
                .currentPassword("0000")
                .newPassword1("9999")
                .newPassword2("9999")
                .build();

        // When
        // Then
        assertThatThrownBy(() -> testContainer.memberService.updatePw(testMember.getId(), request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.USER_WRONG_PASSWORD.getMessage());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 동일한 비밀번호 입력")
    void updatePasswordFailedDueToSameCurrentPassword() {
        // Given
        UpdatePwDTO request = UpdatePwDTO.builder()
                .currentPassword("1234")
                .newPassword1("1234")
                .newPassword2("1234")
                .build();

        // When
        // Then
        assertThatThrownBy(() -> testContainer.memberService.updatePw(testMember.getId(), request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.USER_SAME_PASSWORD.getMessage());
    }

    @Test
    @DisplayName("비밀번호 확인 성공")
    void verifyPasswordSuccessfully() {
        // Given
        VerifyPwDTO request = VerifyPwDTO.builder()
                .password("1234")
                .build();

        // When
        boolean result = testContainer.memberService.verifyPassword(testMember.getId(), request);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("사용자 권한 변경 성공")
    void updateRoleSuccessfully() {
        // Given
        UpdateRoleDTO request = UpdateRoleDTO.builder()
                .employeeId(11111111L)
                .role("ROLE_NORMAL")
                .build();

        // When
        MemberResponse response = testContainer.memberService.updateRole(request);

        // Then
        assertThat(response.role()).isEqualTo("ROLE_NORMAL");
    }

    @Test
    @DisplayName("사용자 권한 변경 실패 - 잘못된 권한 요청")
    void updateRoleFailedForInvalidRole() {
        // Given
        UpdateRoleDTO request = UpdateRoleDTO.builder()
                .employeeId(11111111L)
                .role("INVALID_ROLE")
                .build();

        // When
        // Then
        assertThatThrownBy(() -> testContainer.memberService.updateRole(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.INVALID_ROLE.getMessage());
    }

    @Test
    @DisplayName("회원 등록 성공")
    void registerSuccessfully() {
        // Given
        RegisterDTO request = RegisterDTO.builder()
                .name("김관재")
                .employeeId("11111112")
                .role("ROLE_NORMAL")
                .build();

        // When
        MemberResponse response = testContainer.memberService.register(request);

        // Then
        assertThat(response.name()).isEqualTo("김관재");
        assertThat(response.role()).isEqualTo("ROLE_NORMAL");
    }

    @Test
    @DisplayName("회원 등록 실패 - 중복된 EmployeeId 등록")
    void registerFailedDueToDuplicateEmployeeId() {
        // Given
        RegisterDTO request = RegisterDTO.builder()
                .name("김관재")
                .employeeId("11111111")
                .role("ROLE_NORMAL")
                .build();

        // When
        // Then
        assertThatThrownBy(() -> testContainer.memberService.register(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.USER_ALREADY_EXIST.getMessage());
    }

    @Test
    @DisplayName("회원 존재 여부 확인 성공")
    void existsByEmployeeIdSuccessfully() {
        // When
        boolean exists = testContainer.memberService.existsByEmployeeId("11111111");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("회원 존재 여부 확인 실패")
    void existsByEmployeeIdFailed() {
        // When
        boolean exists = testContainer.memberService.existsByEmployeeId("99999999");

        // Then
        assertThat(exists).isFalse();
    }
}