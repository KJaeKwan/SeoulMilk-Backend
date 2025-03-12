package Seoul_Milk.sm_server.domain.taxInvoice.service;

import Seoul_Milk.sm_server.domain.taxInvoice.dto.TaxInvoiceResponseDTO;
import java.io.ByteArrayInputStream;
import java.util.List;
import Seoul_Milk.sm_server.domain.member.entity.MemberEntity;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

public interface TaxInvoiceService {
    CompletableFuture<TaxInvoiceResponseDTO.Create> processTemplateOcrAsync(MultipartFile image, MemberEntity member);
    CompletableFuture<TaxInvoiceResponseDTO.Create> processTemplateOcrSync(String imageUrl, MemberEntity member, Long imageId);
    Page<TaxInvoiceResponseDTO.GetOne> search(MemberEntity member, String provider, String consumer, String name,
                                              LocalDate startDate, LocalDate endDate, Integer period, String status, int page, int size);
    void delete(Long taxInvoiceId);

    ByteArrayInputStream extractToExcel(List<Long> taxInvoiceIds);
}
