package Seoul_Milk.sm_server.domain.taxInvoice.repository;

import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import Seoul_Milk.sm_server.login.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaxInvoiceJpaRepository extends JpaRepository<TaxInvoice, Long> {
    List<TaxInvoice> findByMemberAndIsTemporaryTrue(MemberEntity member);
}
