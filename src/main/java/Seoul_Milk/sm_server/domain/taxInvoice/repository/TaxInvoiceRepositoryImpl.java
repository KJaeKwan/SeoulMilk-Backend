package Seoul_Milk.sm_server.domain.taxInvoice.repository;

import Seoul_Milk.sm_server.domain.taxInvoice.entity.QTaxInvoice;
import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import Seoul_Milk.sm_server.global.exception.CustomException;
import Seoul_Milk.sm_server.global.exception.ErrorCode;
import Seoul_Milk.sm_server.login.constant.Role;
import Seoul_Milk.sm_server.login.entity.MemberEntity;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

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
    public Page<TaxInvoice> findByProvider(String provider, String employeeId, MemberEntity member, Pageable pageable) {
        QTaxInvoice taxInvoice = QTaxInvoice.taxInvoice;
        BooleanBuilder whereClause = new BooleanBuilder();

        whereClause.and(taxInvoice.ipBusinessName.eq(provider));

        // 일반 사원은 본인 자료만 조회 가능
        if (member.getRole() == Role.ROLE_NORMAL) {
            whereClause.and(taxInvoice.member.employeeId.eq(member.getEmployeeId()));
        }

        // 관리자는 기본적으로 모든 것을 조회 가능하지만, employeeId가 검색 조건으로 들어오면 필터링
        if (member.getRole() == Role.ROLE_ADMIN && employeeId != null && !employeeId.isEmpty()) {
            whereClause.and(taxInvoice.member.employeeId.eq(employeeId));
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
                .where(whereClause)
                .orderBy(taxInvoice.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public Page<TaxInvoice> findByConsumer(String consumer, String employeeId, MemberEntity member, Pageable pageable) {
        QTaxInvoice taxInvoice = QTaxInvoice.taxInvoice;
        BooleanBuilder whereClause = new BooleanBuilder();

        whereClause.and(taxInvoice.suBusinessName.eq(consumer));

        if (member.getRole() == Role.ROLE_NORMAL) {
            whereClause.and(taxInvoice.member.employeeId.eq(member.getEmployeeId()));
        }

        if (member.getRole() == Role.ROLE_ADMIN && employeeId != null && !employeeId.isEmpty()) {
            whereClause.and(taxInvoice.member.employeeId.eq(employeeId));
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
                .where(whereClause)
                .orderBy(taxInvoice.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public Page<TaxInvoice> findByProviderAndConsumer(String provider, String consumer, String employeeId, MemberEntity member, Pageable pageable) {
        QTaxInvoice taxInvoice = QTaxInvoice.taxInvoice;
        BooleanBuilder whereClause = new BooleanBuilder();

        if (provider != null && !provider.isEmpty()) {
            whereClause.and(taxInvoice.ipBusinessName.eq(provider));
        }
        if (consumer != null && !consumer.isEmpty()) {
            whereClause.and(taxInvoice.suBusinessName.eq(consumer));
        }

        if (member.getRole() == Role.ROLE_NORMAL) {
            whereClause.and(taxInvoice.member.employeeId.eq(member.getEmployeeId()));
        }

        if (member.getRole() == Role.ROLE_ADMIN && employeeId != null && !employeeId.isEmpty()) {
            whereClause.and(taxInvoice.member.employeeId.eq(employeeId));
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
                .where(whereClause)
                .orderBy(taxInvoice.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public Page<TaxInvoice> findAll(String employeeId, MemberEntity member, Pageable pageable) {
        QTaxInvoice taxInvoice = QTaxInvoice.taxInvoice;
        BooleanBuilder whereClause = new BooleanBuilder();

        if (member.getRole() == Role.ROLE_NORMAL) {
            whereClause.and(taxInvoice.member.employeeId.eq(member.getEmployeeId()));
        }

        if (member.getRole() == Role.ROLE_ADMIN && employeeId != null && !employeeId.isEmpty()) {
            whereClause.and(taxInvoice.member.employeeId.eq(employeeId));
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
                .where(whereClause)
                .orderBy(taxInvoice.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(results, pageable, total);
    }
}
