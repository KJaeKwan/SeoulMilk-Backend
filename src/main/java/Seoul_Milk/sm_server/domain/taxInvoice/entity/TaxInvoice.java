package Seoul_Milk.sm_server.domain.taxInvoice.entity;

import Seoul_Milk.sm_server.domain.taxInvoice.enums.ArapType;
import Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus;
import Seoul_Milk.sm_server.domain.taxInvoice.enums.TempStatus;
import Seoul_Milk.sm_server.domain.taxInvoiceFile.entity.TaxInvoiceFile;
import Seoul_Milk.sm_server.login.entity.MemberEntity;
import jakarta.persistence.*;
import java.util.Objects;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static Seoul_Milk.sm_server.domain.taxInvoice.enums.ArapType.*;
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
    private String issueId; // 승인번호

    @Enumerated(EnumType.STRING)
    @Column(name = "ARAP", nullable = false)
    private ArapType arap; // 매입, 매출

    @Enumerated(EnumType.STRING)
    @Column(name = "PROGRESS_STATUS", nullable = false)
    private ProcessStatus processStatus;

    @Column(name = "IP_ID", nullable = false, length = 40)
    private String ipId; // 등록번호

    @Column(name = "SU_ID", nullable = false, length = 40)
    private String suId;

    @Column(name = "CHARGE_TOTAL", nullable = false)
    private int chargeTotal; // 총 공급가액

    @Column(name = "TAX_TOTAL")
    private int taxTotal; // 총 세액

    @Column(name = "GRAND_TOTAL")
    private int grandTotal; // 총액

    @Column(name = "ER_DAT", nullable = false, length = 40)
    private String erDat; // 작성일자

    @Column(name = "IP_BUSINESS_NAME")
    private String ipBusinessName; // 상호명

    @Column(name = "SU_ipBusinessName")
    private String suBusinessName;

    @Column(name = "IP_NAME")
    private String ipName; // 대표자명

    @Column(name = "SU_NAME")
    private String suName;

    @Column(name = "IP_ADDRESS")
    private String ipAddress; // 사업체 주소

    @Column(name = "SU_ADDRESS")
    private String suAddress;

    @Column(name = "IP_EMAIL")
    private String ipEmail; // 이메일

    @Column(name = "SU_EMAIL")
    private String suEmail;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "tax_invoice_errors", joinColumns = @JoinColumn(name = "tax_invoice_id"))
    @Column(name = "error_detail")
    @BatchSize(size = 10)
    private List<String> errorDetails = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "IS_TEMPORARY")
    private TempStatus isTemporary;

    @CreatedDate
    @Column(name = "CREATE_AT", updatable = false)
    private LocalDateTime createAt; // 생성일자

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
            int chargeTotal,
            int taxTotal,
            int grandTotal,
            String erDat,
            String ipBusinessName,
            String suBusinessName,
            String ipName,
            String suName,
            String ipAddress,
            String suAddress,
            String ipEmail,
            String suEmail,
            MemberEntity member,
            List<String> errorDetails,
            ProcessStatus processStatus
    ) {
        return TaxInvoice.builder()
                .issueId(issueId)
                .arap(SALES)
                .processStatus(processStatus)
                .ipId(ipId)
                .suId(suId)
                .chargeTotal(chargeTotal)
                .taxTotal(taxTotal)
                .grandTotal(grandTotal)
                .erDat(erDat)
                .ipBusinessName(ipBusinessName)
                .suBusinessName(suBusinessName)
                .ipName(ipName)
                .suName(suName)
                .ipAddress(ipAddress)
                .suAddress(suAddress)
                .ipEmail(ipEmail)
                .suEmail(suEmail)
                .member(member)
                .errorDetails(errorDetails)
                .isTemporary(INITIAL)
                .build();
    }

    public void update(
            String issueId,
            String ipId,
            String suId,
            int chargeTotal,
            int taxTotal,
            int grandTotal,
            String erDat,
            String ipBusinessName,
            String suBusinessName,
            String ipName,
            String suName,
            String ipAddress,
            String suAddress,
            String ipEmail,
            String suEmail,
            MemberEntity member,
            List<String> errorDetails,
            ProcessStatus processStatus
    ){
        this.issueId = issueId;
        this.ipId = ipId;
        this.suId = suId;
        this.chargeTotal = chargeTotal;
        this.taxTotal = taxTotal;
        this.grandTotal = grandTotal;
        this.erDat = erDat;
        this.ipBusinessName = ipBusinessName;
        this.suBusinessName = suBusinessName;
        this.ipName = ipName;
        this.suName = suName;
        this.ipAddress = ipAddress;
        this.suAddress = suAddress;
        this.ipEmail = ipEmail;
        this.suEmail = suEmail;
        this.member = member;
        this.errorDetails = errorDetails;
        this.processStatus = processStatus;
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

    public boolean isYourTaxInvoice(MemberEntity memberEntity){
        return Objects.equals(this.getMember().getId(), memberEntity.getId());
    }
}
