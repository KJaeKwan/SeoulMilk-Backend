package Seoul_Milk.sm_server.domain.member.controller;

import Seoul_Milk.sm_server.domain.member.dto.request.UpdatePwDTO;
import Seoul_Milk.sm_server.domain.member.dto.request.VerifyPwDTO;
import Seoul_Milk.sm_server.domain.member.dto.response.MemberResponse;
import Seoul_Milk.sm_server.domain.member.entity.MemberEntity;
import Seoul_Milk.sm_server.domain.member.enums.Role;
import Seoul_Milk.sm_server.global.common.exception.CustomException;
import Seoul_Milk.sm_server.global.common.exception.ErrorCode;
import Seoul_Milk.sm_server.global.common.response.SuccessResponse;
import Seoul_Milk.sm_server.mock.container.TestContainer;
import Seoul_Milk.sm_server.util.SecurityTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberControllerTest {

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

        testMember = testContainer.memberRepository.save(testMember);
        SecurityTestUtil.setAuthentication(testMember);
    }

    @Test
    @DisplayName("마이페이지 조회 성공")
    void myPageSuccessfully() {
        SuccessResponse<MemberResponse> response = testContainer.memberController.myPage(testMember);

        assertThat(response).isNotNull();
        assertThat(response.getResult().employeeId()).isEqualTo("11111111");
        assertThat(response.getResult().name()).isEqualTo("김재관");
        assertThat(response.getResult().role()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void updatePasswordSuccessfully() {
        UpdatePwDTO request = UpdatePwDTO.builder()
                .currentPassword("1234")
                .newPassword1("9999")
                .newPassword2("9999")
                .build();

        SuccessResponse<String> response = testContainer.memberController.updatePassword(testMember, request);

        assertThat(response).isNotNull();
        assertThat(response.getResult()).isEqualTo("비밀번호 변경에 성공했습니다.");
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 잘못된 현재 비밀번호")
    void updatePasswordFailedDueToWrongCurrentPassword() {
        UpdatePwDTO request = UpdatePwDTO.builder()
                .currentPassword("0000")
                .newPassword1("9999")
                .newPassword2("9999")
                .build();

        assertThatThrownBy(() -> testContainer.memberController.updatePassword(testMember, request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.USER_WRONG_PASSWORD.getMessage());
    }

    @Test
    @DisplayName("비밀번호 확인 성공")
    void verifyPasswordSuccessfully() {
        VerifyPwDTO request = VerifyPwDTO.builder()
                .password("1234")
                .build();

        SuccessResponse<Boolean> response = testContainer.memberController.verifyPassword(testMember, request);

        assertThat(response).isNotNull();
        assertThat(response.getResult()).isTrue();
    }

    @Test
    @DisplayName("비밀번호 확인 성공")
    void verifyPasswordFailDueToWrongPassword() {
        VerifyPwDTO request = VerifyPwDTO.builder()
                .password("9999")
                .build();

        assertThatThrownBy(() -> testContainer.memberController.verifyPassword(testMember, request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.USER_WRONG_PASSWORD.getMessage());
    }

    @Test
    @DisplayName("사번 존재 여부 확인 성공")
    void checkEmployeeNumberExistsSuccessfully() {
        SuccessResponse<Boolean> response = testContainer.memberController.checkEmployeeNumberExists("11111111");

        assertThat(response).isNotNull();
        assertThat(response.getResult()).isTrue();
    }

    @Test
    @DisplayName("사번 존재 여부 확인 실패")
    void checkEmployeeNumberExistsFailed() {
        SuccessResponse<Boolean> response = testContainer.memberController.checkEmployeeNumberExists("99999999");

        assertThat(response).isNotNull();
        assertThat(response.getResult()).isFalse();
    }
}