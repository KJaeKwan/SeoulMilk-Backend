package Seoul_Milk.sm_server.domain.taxValidation.service;

import Seoul_Milk.sm_server.domain.taxValidation.dto.NonVerifiedTaxValidationRequestDTO;
import Seoul_Milk.sm_server.login.entity.MemberEntity;
import java.util.concurrent.ExecutionException;

public interface TaxValidationService {
    String nonVerifiedTaxValidation(NonVerifiedTaxValidationRequestDTO nonVerifiedTaxValidationRequestDTO, MemberEntity memberEntity)
            throws ExecutionException, InterruptedException;
}
