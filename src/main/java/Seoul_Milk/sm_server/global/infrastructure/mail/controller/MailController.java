package Seoul_Milk.sm_server.global.infrastructure.mail.controller;

import Seoul_Milk.sm_server.global.common.response.SuccessResponse;
import Seoul_Milk.sm_server.global.infrastructure.mail.dto.VerifyEmailRequestDTO;
import Seoul_Milk.sm_server.global.infrastructure.mail.dto.VerifyEmailResponseDTO;
import Seoul_Milk.sm_server.global.infrastructure.mail.service.MailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
@Tag(name = "이메일")
public class MailController {
    private final MailService mailService;

    /**
     * Google SMTP 서버를 통해 인증코드를 전송하는 API
     * @param email 전송받을 이메일
     */
    @PostMapping("/verification-requests")
    @Operation(summary = "인증코드 전송")
    public SuccessResponse<String> sendVerificationCode(String email) {

        mailService.sendVerificationCode(email);
        return SuccessResponse.ok(email + "에 인증코드를 전송하였습니다.");
    }

    /**
     * 발송된 인증 코드와 사용자 입력 값 일치 여부를 검증하는 API
     * @param request 이메일, 인증코드 입력
     */
    @PostMapping("/verifications")
    @Operation(summary = "인증 코드 검증")
    public SuccessResponse<VerifyEmailResponseDTO> verifyEmail(VerifyEmailRequestDTO request) {

        VerifyEmailResponseDTO result = mailService.verifyCode(request.email(), request.authCode());
        return SuccessResponse.ok(result);
    }
}
