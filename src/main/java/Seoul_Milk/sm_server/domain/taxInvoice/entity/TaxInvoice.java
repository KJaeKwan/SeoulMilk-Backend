package Seoul_Milk.sm_server.domain.taxInvoice.entity;

import Seoul_Milk.sm_server.domain.taxInvoiceFile.entity.TaxInvoiceFile;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "TAX_INVOICE")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class TaxInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "TAX_INVOICE_ID")
    private Long taxInvoiceId;

    @Column(name = "ISSUE_ID", nullable = false, unique = true, length = 40)
    private String issueId;

    @Column(name = "IP_ID", nullable = false, length = 40)
    private String ipId;

    @Column(name = "SU_ID", nullable = false, length = 40)
    private String suId;

    @Column(name = "TAX_TOTAL", nullable = false)
    private int taxTotal;

    @Column(name = "ER_DAT", nullable = false, length = 40)
    private String erDat;

    @Column(name = "IP_ADDRESS")
    private String ipBusinessName;

    @Column(name = "SU_ADDRESS")
    private String suBusinessName;

    @Column(name = "IP_NAME")
    private String ipName;

    @Column(name = "SU_NAME")
    private String suName;

    @CreatedDate
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "taxInvoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private TaxInvoiceFile file;

    public static TaxInvoice create(
            String issueId,
            String ipId,
            String suId,
            int taxTotal,
            String erDat,
            String ipBusinessName,
            String suBusinessName,
            String ipName,
            String suName
    ) {
        return TaxInvoice.builder()
                .issueId(issueId)
                .ipId(ipId)
                .suId(suId)
                .taxTotal(taxTotal)
                .erDat(erDat)
                .ipBusinessName(ipBusinessName)
                .suBusinessName(suBusinessName)
                .ipName(ipName)
                .suName(suName)
                .build();
    }

    /** 연관관계 편의 메서드 */
    public void attachFile(TaxInvoiceFile file) {
        file.attachTaxInvoice(this);
        this.file = file;
    }
}
