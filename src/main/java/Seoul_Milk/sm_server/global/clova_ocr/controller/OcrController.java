package Seoul_Milk.sm_server.global.clova_ocr.controller;

import Seoul_Milk.sm_server.global.clova_ocr.api.ClovaOcrApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/ocr")
@RequiredArgsConstructor
@Tag(name = "OCR API")
public class OcrController {

    @Value("${clova.ocr.secret-key}")
    private String clovaSecretKey;

    private final ClovaOcrApi clovaOcrApi;

    /**
     * 클라이언트가 Multipart 이미지를 업로드하면 OCR 실행
     * @param file 업로드된 이미지 파일
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "이미지 OCR 에디터")
    public ResponseEntity<?> uploadOcr(@RequestParam(value = "file", required = false) MultipartFile file) {
        long startTime = System.nanoTime();

        List<String> result = clovaOcrApi.callApi("POST", file, clovaSecretKey, file.getContentType());

        long endTime = System.nanoTime(); // 종료 시간 측정
        double elapsedTime = (endTime - startTime) / 1_000_000.0; // 밀리초(ms) 단위 변환
        System.out.println("OCR API 응답 시간: " + elapsedTime + " ms");

        return ResponseEntity.ok(result);
    }
}
