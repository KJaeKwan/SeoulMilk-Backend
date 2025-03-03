package Seoul_Milk.sm_server.domain.taxValidation.service;

import Seoul_Milk.sm_server.domain.taxValidation.dto.TestDTO;
import Seoul_Milk.sm_server.domain.taxValidation.dto.request.NonVerifiedTaxValidationRequestDTO;
import Seoul_Milk.sm_server.domain.taxValidation.dto.response.NonVerifiedTaxValidationResponseDTO;
import Seoul_Milk.sm_server.login.entity.MemberEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface TaxValidationService {
    NonVerifiedTaxValidationResponseDTO nonVerifiedTaxValidation(NonVerifiedTaxValidationRequestDTO nonVerifiedTaxValidationRequestDTO, MemberEntity memberEntity)
            throws ExecutionException, InterruptedException;

    String verifiedTaxValidation(String key)
            throws UnsupportedEncodingException, JsonProcessingException, InterruptedException;
    List<TestDTO> create();
}
