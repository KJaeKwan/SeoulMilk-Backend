package Seoul_Milk.sm_server.mock.repository;

import Seoul_Milk.sm_server.domain.member.entity.MemberEntity;
import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus;
import Seoul_Milk.sm_server.domain.taxInvoice.repository.TaxInvoiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class FakeTaxInvoiceRepository implements TaxInvoiceRepository {

    @Override
    public TaxInvoice getById(Long id) {
        return null;
    }

    @Override
    public Optional<TaxInvoice> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public TaxInvoice save(TaxInvoice taxInvoice) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public Page<TaxInvoice> searchWithFilters(String provider, String consumer, String employeeId, MemberEntity member, LocalDate startDate, LocalDate endDate, ProcessStatus processStatus, Pageable pageable) {
        return null;
    }

    @Override
    public Optional<TaxInvoice> findByIssueId(String issueId) {
        return Optional.empty();
    }

    @Override
    public List<TaxInvoice> findTempInvoicesByMember(MemberEntity member) {
        return null;
    }

    @Override
    public List<TaxInvoice> findTempInvoicesByIds(List<Long> taxInvoiceIds, MemberEntity member) {
        return null;
    }

    @Override
    public List<TaxInvoice> saveAll(List<TaxInvoice> taxInvoices) {
        return null;
    }

    @Override
    public List<TaxInvoice> findAllById(List<Long> taxInvoiceIdList) {
        return null;
    }

    @Override
    public void deleteAll(List<TaxInvoice> taxInvoices) {

    }

    @Override
    public Page<TaxInvoice> searchConsumerOrProvider(String poc, String employeeId, ProcessStatus processStatus, MemberEntity member, Pageable pageable) {
        return null;
    }

    @Override
    public long getProcessStatusCount(ProcessStatus processStatus, MemberEntity member) {
        return 0;
    }

    @Override
    public void deleteOld() {

    }

    @Override
    public void updateIsTemporaryToTemp(List<Long> taxInvoiceIds) {

    }

    @Override
    public boolean existsByIssueId(String issueId) {
        return false;
    }

    @Override
    public boolean isAccessYourTaxInvoice(MemberEntity memberEntity, String issueId) {
        return false;
    }

    @Override
    public boolean isAccessYourTaxInvoice(MemberEntity memberEntity, Long id) {
        return false;
    }

    @Override
    public void updateMandatoryColumns(Long targetId, String issueId, String erDat, String ipId, String suId, int chargeTotal) {

    }
}
