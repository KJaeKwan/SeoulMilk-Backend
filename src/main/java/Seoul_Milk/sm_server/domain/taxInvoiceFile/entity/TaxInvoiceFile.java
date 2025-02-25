package Seoul_Milk.sm_server.domain.taxInvoiceFile.entity;

import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "TAX_INVOICE_FILE")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TaxInvoiceFile {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TAX_INVOICE_ID")
    private TaxInvoice taxInvoice;

    @Column(name = "FILE_URL", unique = true)
    private String fileUrl;

    @Column(name = "FILE_TYPE")
    private String fileType;

    @Column(name = "ORIGINAL_FILE_NAME")
    private String originalFileName;

    @Column(name = "FILE_SIZE")
    private Long fileSize;

    @Column(name = "UPLOAD_DATE")
    private LocalDateTime uploadDate;

    public static TaxInvoiceFile create(
            TaxInvoice taxInvoice,
            String fileUrl,
            String fileType,
            String originalFileName,
            Long fileSize,
            LocalDateTime uploadDate
    ) {
        return TaxInvoiceFile.builder()
                .taxInvoice(taxInvoice)
                .fileUrl(fileUrl)
                .fileType(fileType)
                .originalFileName(originalFileName)
                .fileSize(fileSize)
                .uploadDate(uploadDate)
                .build();
    }


    /** 연관관계 편의 메서드 */
    public void attachTaxInvoice(TaxInvoice taxInvoice) {
        this.taxInvoice = taxInvoice;
    }

}
