package Seoul_Milk.sm_server.domain.taxInvoice.entity;

import Seoul_Milk.sm_server.domain.taxInvoiceFile.entity.TaxInvoiceFile;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "tax_invoice")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TaxInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
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

    @OneToOne(mappedBy = "taxInvoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private TaxInvoiceFile file;

    public static TaxInvoice create(String issueId, String ipId, String suId, int taxTotal, String erDat) {
        return TaxInvoice.builder()
                .issueId(issueId)
                .ipId(ipId)
                .suId(suId)
                .taxTotal(taxTotal)
                .erDat(erDat)
                .build();
    }

    /** 연관관계 편의 메서드 */
    public void attachFile(TaxInvoiceFile file) {
        file.attachTaxInvoice(this);
        this.file = file;
    }
}
