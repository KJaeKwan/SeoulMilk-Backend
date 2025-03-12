package Seoul_Milk.sm_server.domain.taxInvoice.service;

import Seoul_Milk.sm_server.domain.taxInvoice.dto.validation.request.NonVerifiedTaxValidationRequestDTO;
import Seoul_Milk.sm_server.domain.taxInvoice.dto.validation.response.NonVerifiedTaxValidationResponseDTO;
import Seoul_Milk.sm_server.domain.member.entity.MemberEntity;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutionException;

public interface TaxInvoiceValidationService {
    NonVerifiedTaxValidationResponseDTO nonVerifiedTaxValidation(NonVerifiedTaxValidationRequestDTO nonVerifiedTaxValidationRequestDTO, MemberEntity memberEntity)
            throws ExecutionException, InterruptedException;

    String verifiedTaxValidation(String key)
            throws UnsupportedEncodingException, JsonProcessingException, InterruptedException;
}
