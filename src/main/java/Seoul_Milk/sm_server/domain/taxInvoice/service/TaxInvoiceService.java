package Seoul_Milk.sm_server.domain.taxInvoice.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface TaxInvoiceService {
    CompletableFuture<Map<String, Object>> processOcrAsync(MultipartFile image);
    String convertListToJson(List<String> ocrResult);
}
