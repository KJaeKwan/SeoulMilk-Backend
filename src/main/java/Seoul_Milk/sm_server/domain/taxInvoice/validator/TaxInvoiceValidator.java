package Seoul_Milk.sm_server.domain.taxInvoice.validator;

import static Seoul_Milk.sm_server.global.exception.ErrorCode.DO_NOT_ACCESS_OTHER_TAX_INVOICE;
import static Seoul_Milk.sm_server.global.exception.ErrorCode.TAX_INVOICE_NOT_EXIST;

import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import Seoul_Milk.sm_server.global.exception.CustomException;
import Seoul_Milk.sm_server.login.entity.MemberEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * TaxInvoice 엔티티 검증클래스
 * 유저가 테이블에(존재하지 않는 pk값) 없는 엔티티를 가져오려고 하거나
 * 다른 유저의 TaxInvoice값을 가져오려고 할때 Exception발생
 */
@Component
@RequiredArgsConstructor
public class TaxInvoiceValidator {

    /**
     * 세금계산서 존재 여부 검증
     */
    public void validateExistence(List<TaxInvoice> taxInvoices, List<Long> requestedIds) {
        if (taxInvoices.size() != requestedIds.size()) {
            throw new CustomException(TAX_INVOICE_NOT_EXIST);
        }
    }

    /**
     * 사용자가 해당 세금계산서를 조작할 권한이 있는지 검증
     */
    public void validateOwnership(MemberEntity memberEntity, List<TaxInvoice> taxInvoices) {
        boolean hasUnauthorizedAccess = taxInvoices.stream()
                .anyMatch(invoice -> !invoice.getMember().getId().equals(memberEntity.getId()));

        if (hasUnauthorizedAccess) {
            throw new CustomException(DO_NOT_ACCESS_OTHER_TAX_INVOICE);
        }
    }
}
