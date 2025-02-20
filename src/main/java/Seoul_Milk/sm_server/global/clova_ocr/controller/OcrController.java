package Seoul_Milk.sm_server.global.clova_ocr.controller;

import Seoul_Milk.sm_server.global.clova_ocr.api.ClovaOcrApi;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class OcrController {

    @Value("${clova.ocr.secret-key}")
    private String clova_secret_key;

    private final ClovaOcrApi clovaOcrApi;

    /**
     * 해당 경로로 Get 요청이 오면 classpath에 있는 fileName 파일을 CLOVA OCR 요청
     */
    @GetMapping("/naverOcr")
    public ResponseEntity ocr() throws IOException {
        String fileName = "tax1.png";
        File file = ResourceUtils.getFile("classpath:static/image/test/"+fileName);

        List<String> result = clovaOcrApi.callApi("POST", file.getPath(), clova_secret_key, "png");
        return new ResponseEntity(result, HttpStatus.OK);
    }
}
