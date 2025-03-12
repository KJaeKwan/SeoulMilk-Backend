package Seoul_Milk.sm_server.global.infrastructure.upload.util;

import Seoul_Milk.sm_server.global.common.exception.CustomException;
import Seoul_Milk.sm_server.global.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.List;
import java.util.UUID;

@Component
public class AwsS3Util {

    // 파일명 중복 방지 (UUID)
    public String createFileName(String folderName, String fileName, boolean isImage) {
        String extension = getFileExtension(fileName, isImage);

        int lastDotIndex = fileName.lastIndexOf(".");
        String baseName = (lastDotIndex == -1) ? fileName : fileName.substring(0, lastDotIndex);

        // 한글 및 특수문자 처리: Unicode 정규화 (NFC)
        String normalizedFileName = Normalizer.normalize(baseName, Normalizer.Form.NFC);

        // 공백 및 특수문자 제거 (한글, 영어, 숫자만 허용)
        String safeFileName = normalizedFileName.replaceAll("[^a-zA-Z0-9가-힣]", "_");

        String uniqueFileName = safeFileName + "_" + UUID.randomUUID() + "." + extension;

        return folderName + "/" + uniqueFileName;
    }

    // 파일 유효성 검사
    public String getFileExtension(String fileName, boolean isImage) {
        fileName = fileName.trim();

        // 확장자 추출
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

        List<String> imageExtensions = List.of("jpg", "jpeg", "png", "pdf");
        List<String> documentExtensions = List.of("pdf", "pptx", "hwp", "docx", "xlsx", "txt", "csv", "zip");

        // 리스트에서 확장자 포함 여부 확인
        boolean isValidImage = imageExtensions.contains(fileExtension);
        boolean isValidDocument = documentExtensions.contains(fileExtension);

        if (isImage) {
            if (!isValidImage) {
                throw new CustomException(ErrorCode.INVALID_REQUEST);
            }
            return fileExtension;
        } else {
            if (!isValidDocument) {
                throw new CustomException(ErrorCode.INVALID_REQUEST);
            }
        }

        return fileExtension;
    }

    // S3에서 파일 키 추출
    public String extractS3Key(String bucket, String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        String prefix = "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/";
        if (!fileUrl.startsWith(prefix)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        return fileUrl.substring(prefix.length());
    }
}
