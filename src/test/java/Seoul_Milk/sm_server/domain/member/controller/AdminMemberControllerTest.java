package Seoul_Milk.sm_server.domain.member.controller;

import Seoul_Milk.sm_server.domain.member.dto.request.RegisterDTO;
import Seoul_Milk.sm_server.domain.member.dto.request.UpdateRoleDTO;
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

class AdminMemberControllerTest {

    private TestContainer testContainer;

    @BeforeEach
    void setUp() {
        testContainer = new TestContainer();

        MemberEntity testAdmin = MemberEntity.createVerifiedMember(
                "11111111",
                "김재관",
                new BCryptPasswordEncoder().encode("1234"),
                Role.ROLE_ADMIN
        );

        MemberEntity testMember = MemberEntity.createVerifiedMember(
                "11111112",
                "김관재",
                new BCryptPasswordEncoder().encode("1234"),
                Role.ROLE_NORMAL
        );

        testAdmin = testContainer.memberRepository.save(testAdmin);
        testMember = testContainer.memberRepository.save(testMember);
        SecurityTestUtil.setAuthentication(testAdmin);
    }

    @Test
    @DisplayName("사원 등록 성공")
    void tmpRegisterMemberSuccessfully() {
        RegisterDTO request = RegisterDTO.builder()
                .employeeId("11111113")
                .name("테스트")
                .role("ROLE_NORMAL")
                .build();

        SuccessResponse<MemberResponse> response = testContainer.adminMemberController.tmpRegisterMember(request);

        assertThat(response).isNotNull();
        assertThat(response.getResult().employeeId()).isEqualTo("11111113");
        assertThat(response.getResult().name()).isEqualTo("테스트");
        assertThat(response.getResult().role()).isEqualTo("ROLE_NORMAL");
    }

    @Test
    @DisplayName("사원 등록 실패 - 사번 중복")
    void tmpRegisterMemberFailedDueToDuplicateEmployeeId() {
        RegisterDTO request = RegisterDTO.builder()
                .employeeId("11111112")
                .name("중복사용자")
                .role("ROLE_NORMAL")
                .build();

        assertThatThrownBy(() -> testContainer.adminMemberController.tmpRegisterMember(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.USER_ALREADY_EXIST.getMessage());
    }

    @Test
    @DisplayName("사용자 권한 변경 성공")
    void updateRoleSuccessfully() {
        UpdateRoleDTO request = UpdateRoleDTO.builder()
                .employeeId(11111111L)
                .role("ROLE_NORMAL")
                .build();

        SuccessResponse<MemberResponse> response = testContainer.adminMemberController.updateRole(request);

        assertThat(response).isNotNull();
        assertThat(response.getResult().role()).isEqualTo("ROLE_NORMAL");
    }
}
