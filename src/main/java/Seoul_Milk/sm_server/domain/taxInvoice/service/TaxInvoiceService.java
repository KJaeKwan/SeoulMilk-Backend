package Seoul_Milk.sm_server.domain.taxInvoice.service;

import Seoul_Milk.sm_server.domain.member.entity.MemberEntity;
import Seoul_Milk.sm_server.domain.taxInvoice.dto.TaxInvoiceResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface TaxInvoiceService {
    CompletableFuture<TaxInvoiceResponseDTO.Create> processTemplateOcrAsync(MultipartFile image, MemberEntity member);
    CompletableFuture<TaxInvoiceResponseDTO.Create> processTemplateOcrSync(String imageUrl, MemberEntity member, Long imageId);
    Page<TaxInvoiceResponseDTO.GetOne> search(MemberEntity member, String provider, String consumer, String name,
                                              LocalDate startDate, LocalDate endDate, Integer period, String status, int page, int size);
    void delete(Long taxInvoiceId);
    void deleteOldTaxInvoices();
    ByteArrayInputStream extractToExcel(List<Long> taxInvoiceIds);
}
