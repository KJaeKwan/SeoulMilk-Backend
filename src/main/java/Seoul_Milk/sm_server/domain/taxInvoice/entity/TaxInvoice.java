package Seoul_Milk.sm_server.domain.taxInvoice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tax_invoice")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TaxInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tax_invoice_id")
    private Long taxInvoiceId;

    @Column(name = "issue_id", nullable = false, unique = true, length = 40)
    private String issueId;

    @Column(name = "ip_id", nullable = false, length = 40)
    private String ipId;

    @Column(name = "su_id", nullable = false, length = 40)
    private String suId;

    @Column(name = "tax_total", nullable = false)
    private int taxTotal;

    @Column(name = "er_dat", nullable = false, length = 40)
    private String erDat;
}
