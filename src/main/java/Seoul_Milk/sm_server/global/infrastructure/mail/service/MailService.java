package Seoul_Milk.sm_server.global.infrastructure.mail.service;

import Seoul_Milk.sm_server.global.common.exception.CustomException;
import Seoul_Milk.sm_server.global.common.exception.ErrorCode;
import Seoul_Milk.sm_server.global.infrastructure.mail.dto.VerifyEmailResponseDTO;
import Seoul_Milk.sm_server.global.infrastructure.redis.RedisUtils;
import Seoul_Milk.sm_server.domain.member.repository.MemberRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private static final String AUTH_CODE_PREFIX = "AuthCode:";
    private static final String AUTH_REQUEST_PREFIX = "AuthRequest:";

    private final JavaMailSender mailSender;
    private final RedisUtils redisUtils;
    private final MemberRepository memberRepository;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.mail_auth_code_expiration}")
    private long authCodeExpirationMillis;

    @Value("${spring.mail.username}")
    private String mailSenderAddress;

    /**
     * 이메일 인증 코드 전송
     */
    @Async("mailTaskExecutor")
    public void sendVerificationCode(String email) {
        if (memberRepository.findByEmail(email).isPresent()) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXIST);
        }

        String title = "서울 우유 이메일 인증 번호";
        String authCode = generateAuthCode();

        // 5분 내에 인증 코드 요청이 있었는지 확인
        String lastRequestTime = redisUtils.getValues(AUTH_REQUEST_PREFIX + email);
        if (lastRequestTime != null) {
            throw new CustomException(ErrorCode.EMAIL_REQUEST_LIMIT_EXCEEDED);
        }

        try {
            sendMail(email, title, authCode);

            // Redis에 인증 코드 저장 (key: AuthCode + email, value: authCode)
            redisUtils.setValues(
                    AUTH_CODE_PREFIX + email,
                    authCode,
                    Duration.ofMillis(authCodeExpirationMillis)
            );

            redisUtils.setValues(AUTH_REQUEST_PREFIX + email,
                    "REQUESTED",
                    Duration.ofMinutes(5)
            );


            log.info("이메일 인증 코드 전송 성공: {}", email);
        } catch (Exception e) {
            log.error("이메일 전송 실패: email={}, error={}", email, e.getMessage(), e);

            // 이메일 전송 실패 시 Redis에 저장된 인증 코드 삭제
            redisUtils.deleteValues(AUTH_CODE_PREFIX + email);

            throw new CustomException(ErrorCode.EMAIL_SEND_FAIL);
        }
    }

    /**
     * 이메일 전송
     */
    private void sendMail(String toEmail, String title, String authCode) throws MessagingException {
        MimeMessage emailForm = createEmailForm(toEmail, title, authCode);
        mailSender.send(emailForm);
    }

    /**
     * 이메일 템플릿 생성
     */
    private MimeMessage createEmailForm(String toEmail, String title, String authCode) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        // Thymeleaf 템플릿 처리
        Context context = new Context();
        context.setVariable("verificationCode", authCode);
        String htmlContent = templateEngine.process("verification-email", context);

        helper.setTo(toEmail);
        helper.setSubject(title);
        helper.setText(htmlContent, true);
        helper.setFrom(mailSenderAddress);

        log.info("발신자 이메일 주소: {}", mailSenderAddress);
        return message;
    }

    // 이메일 인증 코드 검증
    public VerifyEmailResponseDTO verifyCode(String email, String authCode) {
        if (memberRepository.findByEmail(email).isPresent()) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXIST);
        }

        String redisAuthCode = redisUtils.getValues(AUTH_CODE_PREFIX + email);

        if (redisAuthCode == null || !redisAuthCode.equals(authCode)) {
            log.warn("이메일 인증 실패: email={}, 입력 코드={}, Redis 코드={}", email, authCode, redisAuthCode);
            throw new CustomException(ErrorCode.EMAIL_AUTH_FAIL);
        }

        log.info("이메일 인증 성공: email={}", email);
        return VerifyEmailResponseDTO.of(true);
    }

    // 랜덤 인증 코드 6자리 생성
    private String generateAuthCode() {
        return new SecureRandom().ints(6, 0, 10)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining());
    }
}
