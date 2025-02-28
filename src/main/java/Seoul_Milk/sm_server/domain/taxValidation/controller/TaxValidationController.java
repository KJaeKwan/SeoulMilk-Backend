package Seoul_Milk.sm_server.domain.taxValidation.controller;

import Seoul_Milk.sm_server.domain.taxValidation.dto.NonVerifiedTaxValidationRequestDTO;
import Seoul_Milk.sm_server.domain.taxValidation.service.TaxValidationService;
import Seoul_Milk.sm_server.domain.taxValidation.shared.SharedData;
import Seoul_Milk.sm_server.global.annotation.CurrentMember;
import Seoul_Milk.sm_server.global.dto.response.SuccessResponse;
import Seoul_Milk.sm_server.login.entity.MemberEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/valid")
@Tag(name = "진위여부 검사 API")
public class TaxValidationController {
    private final TaxValidationService taxValidationService;

    @Operation(summary = "간편인증 안한 상태에서의 진위여부 검사 api")
    @PostMapping("/non-verified")
    public SuccessResponse<String> nonVerifiedTaxValidation
            (@RequestBody NonVerifiedTaxValidationRequestDTO nvtv, @CurrentMember MemberEntity memberEntity) throws ExecutionException, InterruptedException {
        return SuccessResponse.ok(taxValidationService.nonVerifiedTaxValidation(nvtv, memberEntity));
    }

    /**
     * 공유데이터 테스트용
     * TODO 배포전 지우기
     * @return
     */
    @GetMapping("/getSharedData")
    public String getSharedData() {
        return "JOB_INDEX: " + SharedData.JOB_INDEX +
                ", THREAD_INDEX: " + SharedData.THREAD_INDEX +
                ", JTI: " + SharedData.JTI +
                ", TWO_WAY_TIMESTAMP: " + SharedData.TWO_WAY_TIMESTAMP;
    }
}
