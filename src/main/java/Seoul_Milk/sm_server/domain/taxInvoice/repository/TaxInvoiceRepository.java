package Seoul_Milk.sm_server.domain.taxInvoice.repository;

import Seoul_Milk.sm_server.domain.member.entity.MemberEntity;
import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus;
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
    Optional<TaxInvoice> findByIssueId(String issueId);

    // 임시 저장 관련
    List<TaxInvoice> findTempInvoicesByMember(MemberEntity member);
    List<TaxInvoice> findTempInvoicesByIds(List<Long> taxInvoiceIds, MemberEntity member);
    List<TaxInvoice> saveAll(List<TaxInvoice> taxInvoices);
    List<TaxInvoice> findAllById(List<Long> taxInvoiceIdList);

    void deleteAll(List<TaxInvoice> taxInvoices);
    Page<TaxInvoice> searchConsumerOrProvider(String poc, String employeeId, ProcessStatus processStatus, MemberEntity member, Pageable pageable);
    long getProcessStatusCount(ProcessStatus processStatus, MemberEntity member);
    void deleteOld();

    //임시저장 상태를 모두 TEMP로 바꾸기
    void updateIsTemporaryToTemp(List<Long> taxInvoiceIds);
    boolean existsByIssueId(String issueId);

    /**
     * 현재 유저가 본인의 TaxInvoice데이터에 접근하려고 하고 있는지 확인하는 로직
     * 진위여부 파악하는 것도 처리상태를 바꾸는 로직이 포함되어있어서 본인것이 아닌 세금계산서를 바꿀 수 있는 가능성이
     * 있기 때문
     */
    boolean isAccessYourTaxInvoice(MemberEntity memberEntity, String issueId);
    boolean isAccessYourTaxInvoice(MemberEntity memberEntity, Long id);

    /**
     * 필수컬럼 값 수정
     */
    void updateMandatoryColumns(Long targetId, String issueId, String erDat, String ipId, String suId, int chargeTotal);
}

