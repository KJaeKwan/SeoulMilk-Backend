package Seoul_Milk.sm_server.domain.taxValidation.controller;

import Seoul_Milk.sm_server.domain.taxValidation.dto.NonVerifiedTaxValidationRequestDTO;
import Seoul_Milk.sm_server.domain.taxValidation.dto.NonVerifiedTaxValidationResponseDTO;
import Seoul_Milk.sm_server.domain.taxValidation.dto.VerifiedTaxValidationRequestDTO;
import Seoul_Milk.sm_server.domain.taxValidation.service.TaxValidationService;
import Seoul_Milk.sm_server.global.annotation.CurrentMember;
import Seoul_Milk.sm_server.global.dto.response.SuccessResponse;
import Seoul_Milk.sm_server.login.entity.MemberEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
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
    public SuccessResponse<NonVerifiedTaxValidationResponseDTO> nonVerifiedTaxValidation
            (@RequestBody NonVerifiedTaxValidationRequestDTO nvtv, @CurrentMember MemberEntity memberEntity) throws ExecutionException, InterruptedException {
        return SuccessResponse.ok(taxValidationService.nonVerifiedTaxValidation(nvtv, memberEntity));
    }

    @Operation(summary = "간편인증을 한 상태에서의 진위여부 검사 api")
    @PostMapping("/verified")
    public SuccessResponse<String> verifiedTaxvalidation(@RequestBody VerifiedTaxValidationRequestDTO validationResponseDTO)
            throws UnsupportedEncodingException, JsonProcessingException, InterruptedException {
        return SuccessResponse.ok(taxValidationService.verifiedTaxValidation(validationResponseDTO.getKey()));
    }
}
