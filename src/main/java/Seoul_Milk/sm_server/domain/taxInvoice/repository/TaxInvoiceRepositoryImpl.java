package Seoul_Milk.sm_server.domain.taxInvoice.repository;

import Seoul_Milk.sm_server.domain.taxInvoice.entity.QTaxInvoice;
import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus;
import Seoul_Milk.sm_server.domain.taxInvoiceFile.entity.QTaxInvoiceFile;
import Seoul_Milk.sm_server.global.exception.CustomException;
import Seoul_Milk.sm_server.global.exception.ErrorCode;
import Seoul_Milk.sm_server.login.constant.Role;
import Seoul_Milk.sm_server.login.entity.MemberEntity;
import Seoul_Milk.sm_server.login.entity.QMemberEntity;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TaxInvoiceRepositoryImpl implements TaxInvoiceRepository {

    private final TaxInvoiceJpaRepository taxInvoiceJpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public TaxInvoice getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.TAX_INVOICE_NOT_EXIST));
    }

    @Override
    public Optional<TaxInvoice> findById(Long id) {
        return taxInvoiceJpaRepository.findById(id);
    }

    @Override
    public TaxInvoice save(TaxInvoice taxInvoice) {
        return taxInvoiceJpaRepository.save(taxInvoice);
    }

    @Override
    public void delete(Long id) {
        taxInvoiceJpaRepository.deleteById(id);
    }

    @Override
    public Page<TaxInvoice> searchWithFilters(String provider, String consumer, String employeeId, MemberEntity member,
                                              LocalDate startDate, LocalDate endDate, ProcessStatus status, Pageable pageable) {
        QTaxInvoice taxInvoice = QTaxInvoice.taxInvoice;
        QMemberEntity memberEntity = QMemberEntity.memberEntity;
        QTaxInvoiceFile taxInvoiceFile = QTaxInvoiceFile.taxInvoiceFile;

        BooleanBuilder whereClause = new BooleanBuilder();

        // 권한 조건
        if (member.getRole() == Role.ROLE_NORMAL) {
            whereClause.and(taxInvoice.member.employeeId.eq(member.getEmployeeId()));
        }
        if (member.getRole() == Role.ROLE_ADMIN && employeeId != null && !employeeId.isEmpty()) {
            whereClause.and(taxInvoice.member.employeeId.eq(employeeId));
        }

        // 공급자 검색 조건
        if (provider != null && !provider.isEmpty()) {
            whereClause.and(taxInvoice.ipName.eq(provider));
        }

        // 공급받는자 검색 조건
        if (consumer != null && !consumer.isEmpty()) {
            whereClause.and(taxInvoice.suName.eq(consumer));
        }

        // 날짜 검색 (특정 날짜 or 최근 N개월 내)
        if (startDate != null && endDate != null) {
            whereClause.and(taxInvoice.createdAt.between(startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX)));
        }

        // 승인 상태 조건
        if (status != null) {
            whereClause.and(taxInvoice.processStatus.eq(status));
        }

        long total = Optional.ofNullable(
                queryFactory
                        .select(Wildcard.count)
                        .from(taxInvoice)
                        .where(whereClause)
                        .fetchOne()
        ).orElse(0L);

        List<TaxInvoice> results = queryFactory
                .selectFrom(taxInvoice)
                .leftJoin(taxInvoice.member, memberEntity).fetchJoin()
                .leftJoin(taxInvoice.file, taxInvoiceFile).fetchJoin()
                .where(whereClause)
                .orderBy(taxInvoice.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public List<TaxInvoice> findTempInvoicesByMember(MemberEntity member) {
        QTaxInvoice taxInvoice = QTaxInvoice.taxInvoice;

        return queryFactory.selectFrom(taxInvoice)
                .where(
                        taxInvoice.member.eq(member),
                        taxInvoice.isTemporary.isTrue()
                )
                .fetch();
    }

    @Override
    public List<TaxInvoice> findTempInvoicesByIds(List<Long> taxInvoiceIds, MemberEntity member) {
        QTaxInvoice taxInvoice = QTaxInvoice.taxInvoice;

        return queryFactory.selectFrom(taxInvoice)
                .where(
                        taxInvoice.taxInvoiceId.in(taxInvoiceIds),
                        taxInvoice.member.eq(member)
                )
                .fetch();
    }

    @Override
    public List<TaxInvoice> saveAll(List<TaxInvoice> taxInvoices) {
        return taxInvoiceJpaRepository.saveAll(taxInvoices);
    }

    @Override
    public TaxInvoice findByIssueId(String issueId) {
        return taxInvoiceJpaRepository.findByIssueId(issueId);
    }
}
