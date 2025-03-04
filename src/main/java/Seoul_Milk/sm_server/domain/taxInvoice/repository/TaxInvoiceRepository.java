package Seoul_Milk.sm_server.domain.taxInvoice.repository;

import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus;
import Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.dto.TaxInvoiceSearchResult;
import Seoul_Milk.sm_server.login.entity.MemberEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaxInvoiceRepository {
    TaxInvoice getById(Long id);
    Optional<TaxInvoice> findById(Long id);
    TaxInvoice save(TaxInvoice taxInvoice);
    void delete(Long id);
    Page<TaxInvoice> searchWithFilters(String provider, String consumer, String employeeId, MemberEntity member, LocalDate startDate, LocalDate endDate, ProcessStatus processStatus, Pageable pageable);
    TaxInvoice findByIssueId(String issueId);

    // 임시 저장 관련
    List<TaxInvoice> findTempInvoicesByMember(MemberEntity member);
    List<TaxInvoice> findTempInvoicesByIds(List<Long> taxInvoiceIds, MemberEntity member);
    List<TaxInvoice> saveAll(List<TaxInvoice> taxInvoices);
    List<TaxInvoice> findAll();
    Page<TaxInvoice> searchConsumerOrProvider(String poc, String employeeId, ProcessStatus processStatus, MemberEntity member, Pageable pageable);
    long getProcessStatusCount(ProcessStatus processStatus, MemberEntity member);
}

