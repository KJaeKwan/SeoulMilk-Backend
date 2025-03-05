package Seoul_Milk.sm_server.global.upload.service;

import Seoul_Milk.sm_server.global.exception.CustomException;
import Seoul_Milk.sm_server.global.exception.ErrorCode;
import Seoul_Milk.sm_server.global.upload.util.AwsS3Util;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AwsS3Service {
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3 s3Client;
    private final AwsS3Util awsS3Util;

    // 여러 개의 파일 업로드
    public List<String> uploadFiles(String folderName, List<MultipartFile> files, boolean isImage) {
        return uploadFilesToFolder(folderName, files, isImage);
    }

    // 단일 파일 업로드
    public String uploadFile(String folderName, MultipartFile file, boolean isImage) {
        return uploadFileToFolder(folderName, file, isImage);
    }

    // 공통 - 여러개의 파일 업로드
    private List<String> uploadFilesToFolder(String folderName, List<MultipartFile> files, boolean isImage) {
        long startTime = System.currentTimeMillis(); // 업로드 시작 시간
        List<String> fileUrlList = new ArrayList<>();

        files.forEach(file -> {

            String fileName = awsS3Util.createFileName(folderName, file.getOriginalFilename(), isImage);

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(file.getSize());
            objectMetadata.setContentType(file.getContentType());

            try (InputStream inputStream = file.getInputStream()) {
                s3Client.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
                        .withCannedAcl(CannedAccessControlList.PublicRead));
            } catch (IOException e) {
                throw new CustomException(ErrorCode.UPLOAD_FAILED);
            }

            String fileUrl = "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + fileName;
            fileUrlList.add(fileUrl);
        });

        long endTime = System.currentTimeMillis(); // 전체 업로드 완료 시간
        long totalDuration = endTime - startTime; // 전체 업로드 소요 시간

        System.out.println("전체 파일 업로드 소요 시간: " + totalDuration + "ms");

        return fileUrlList;
    }

    // 공통 - 파일 하나 업로드
    private String uploadFileToFolder(String folderName, MultipartFile file, boolean isImage) {

        String fileName = awsS3Util.createFileName(folderName, file.getOriginalFilename(), isImage);

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(file.getSize());
        objectMetadata.setContentType(file.getContentType());

        try (InputStream inputStream = file.getInputStream()) {
            s3Client.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (IOException e) {
            throw new CustomException(ErrorCode.UPLOAD_FAILED);
        }

        return "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + fileName;
    }


    // 파일 삭제
    public void deleteFile(String fileUrl) {
        try {
            URI uri = new URI(fileUrl);
            String fileKey = uri.getPath().substring(1); // 첫 번째 "/" 제거

            System.out.println("삭제할 파일 경로: " + fileKey); // 디버깅용

            s3Client.deleteObject(new DeleteObjectRequest(bucket, fileKey));
            System.out.println("S3 파일 삭제 완료: " + fileKey);

        } catch (Exception e) {
            System.err.println("[ERROR] S3 파일 삭제 실패: " + e.getMessage());
            throw new CustomException(ErrorCode.DELETE_FAILED);
        }
    }


}
