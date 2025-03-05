package Seoul_Milk.sm_server.domain.taxInvoice.entity;

import Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus;
import Seoul_Milk.sm_server.domain.taxInvoice.enums.TempStatus;
import Seoul_Milk.sm_server.domain.taxInvoiceFile.entity.TaxInvoiceFile;
import Seoul_Milk.sm_server.login.entity.MemberEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus.*;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.TempStatus.INITIAL;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "PROGRESS_STATUS", nullable = false)
    private ProcessStatus processStatus;

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

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "tax_invoice_errors", joinColumns = @JoinColumn(name = "tax_invoice_id"))
    @Column(name = "error_detail")
    @BatchSize(size = 10)
    private List<String> errorDetails = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "IS_TEMPORARY")
    private TempStatus isTemporary = INITIAL;

    @CreatedDate
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "taxInvoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private TaxInvoiceFile file;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID", nullable = false)
    private MemberEntity member;

    public static TaxInvoice create(
            String issueId,
            String ipId,
            String suId,
            int taxTotal,
            String erDat,
            String ipBusinessName,
            String suBusinessName,
            String ipName,
            String suName,
            MemberEntity member,
            List<String> errorDetails
    ) {
        return TaxInvoice.builder()
                .processStatus(UNAPPROVED) // default 값 unapproved(미승인)
                .issueId(issueId)
                .ipId(ipId)
                .suId(suId)
                .taxTotal(taxTotal)
                .erDat(erDat)
                .ipBusinessName(ipBusinessName)
                .suBusinessName(suBusinessName)
                .ipName(ipName)
                .suName(suName)
                .member(member)
                .errorDetails(errorDetails)
                .isTemporary(INITIAL)
                .build();
    }

    /** 연관관계 편의 메서드 */
    public void attachFile(TaxInvoiceFile file) {
        file.attachTaxInvoice(this);
        this.file = file;
    }

    public void attachMember(MemberEntity member) {
        this.member = member;
    }

    /** 승인 처리 */
    public void approve() {
        this.processStatus = APPROVED;
    }

    /** 반려 처리 */
    public void reject() {
        this.processStatus = REJECTED;
    }

    /** 임시 저장 여부 변경 **/
    public void updateIsTemp(TempStatus isTemporary) {
        this.isTemporary = isTemporary;
    }
}
