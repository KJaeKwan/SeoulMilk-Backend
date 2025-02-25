package Seoul_Milk.sm_server.domain.taxInvoiceFile.entity;

import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "tax_invoice_file")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TaxInvoiceFile {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_invoice_id")
    private TaxInvoice taxInvoice;

    private String fileUrl;
    private String fileType;
    private String originalFileName;
    private Long fileSize;
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
