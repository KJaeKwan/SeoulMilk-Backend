package Seoul_Milk.sm_server.global.upload.controller;

import Seoul_Milk.sm_server.global.dto.response.SuccessResponse;
import Seoul_Milk.sm_server.global.upload.service.AwsS3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Tag(name = "이미지 업로드 API")
public class UploadController {

    private final AwsS3Service awsS3Service;

    // TODO 테스트 후 삭제 요망
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "이미지 업로드 에디터. 이미지가 S3에 성공적으로 올라가는지 테스트용 API")
    public SuccessResponse<String> upload(
            @RequestPart(required = false) MultipartFile image) {

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = awsS3Service.uploadFile("image-editor", image, true);
        }

        return SuccessResponse.ok(imageUrl);
    }
}
