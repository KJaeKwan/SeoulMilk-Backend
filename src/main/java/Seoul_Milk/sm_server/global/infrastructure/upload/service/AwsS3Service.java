package Seoul_Milk.sm_server.global.infrastructure.upload.service;

import Seoul_Milk.sm_server.global.common.exception.CustomException;
import Seoul_Milk.sm_server.global.common.exception.ErrorCode;
import Seoul_Milk.sm_server.global.infrastructure.upload.util.AwsS3Util;
import Seoul_Milk.sm_server.global.common.util.CustomMultipartFile;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

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

    private final AwsS3Util awsS3Util;
    private final S3Client s3Client;

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
        long startTime = System.currentTimeMillis();
        List<String> fileUrlList = new ArrayList<>();

        files.forEach(file -> {
            String fileName = awsS3Util.createFileName(folderName, file.getOriginalFilename(), isImage);

            try (InputStream inputStream = file.getInputStream()) {
                // 파일 업로드
                s3Client.putObject(PutObjectRequest.builder()
                                .bucket(bucket)
                                .key(fileName)
                                .acl(ObjectCannedACL.PUBLIC_READ)
                                .contentType(file.getContentType())
                                .build(),
                        RequestBody.fromInputStream(inputStream, file.getSize())
                );
            } catch (IOException e) {
                throw new CustomException(ErrorCode.UPLOAD_FAILED);
            }

            String fileUrl = "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + fileName;
            fileUrlList.add(fileUrl);
        });

        long endTime = System.currentTimeMillis();
        long totalDuration = endTime - startTime;
        System.out.println("전체 파일 업로드 소요 시간: " + totalDuration + "ms");

        return fileUrlList;
    }

    // 공통 - 파일 하나 업로드
    private String uploadFileToFolder(String folderName, MultipartFile file, boolean isImage) {
        String fileName = awsS3Util.createFileName(folderName, file.getOriginalFilename(), isImage);

        try (InputStream inputStream = file.getInputStream()) {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(fileName)
                            .acl(ObjectCannedACL.PUBLIC_READ)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromInputStream(inputStream, file.getSize())
            );
        } catch (IOException e) {
            throw new CustomException(ErrorCode.UPLOAD_FAILED);
        }

        return "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + fileName;
    }


    /**
     * 파일 삭제
     */
    public void deleteFile(String fileUrl) {
        try {
            URI uri = new URI(fileUrl);
            String fileKey = uri.getPath().substring(1);

            System.out.println("삭제할 파일 경로: " + fileKey);
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileKey)
                    .build()
            );
            System.out.println("S3 파일 삭제 완료: " + fileKey);

        } catch (Exception e) {
            System.err.println("[ERROR] S3 파일 삭제 실패: " + e.getMessage());
            throw new CustomException(ErrorCode.DELETE_FAILED);
        }
    }


    /**
     * S3에서 파일 다운로드 후 MultipartFile로 변환
     */
    public MultipartFile downloadFileFromS3(String fileUrl) {
        try {
            System.out.println("[DEBUG] S3에서 파일 다운로드 시작: " + fileUrl);

            // URL에서 버킷과 키 추출
            String bucketName = extractBucketName(fileUrl);
            String fileKey = extractFileKey(fileUrl);

            // S3에서 파일 다운로드 요청
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
            byte[] fileData = IOUtils.toByteArray(s3Object);
            String contentType = s3Object.response().contentType();

            System.out.println("[DEBUG] S3 파일 다운로드 완료: " + fileKey);

            // MultipartFile로 변환
            return new CustomMultipartFile(fileData, fileKey, contentType);

        } catch (IOException e) {
            System.out.println("[ERROR] S3 파일 다운로드 실패: " + e.getMessage());
            throw new RuntimeException("S3 파일 다운로드 실패", e);
        }
    }

    // 파일 이동
    public String moveFileToFinalFolder(String fileUrl, String destinationFolder) {
        String sourceKey = awsS3Util.extractS3Key(bucket, fileUrl);
        String fileName = sourceKey.substring(sourceKey.lastIndexOf("/") + 1);
        String destinationKey = destinationFolder + "/" + fileName;

        try {
            // S3 객체 복사
            s3Client.copyObject(CopyObjectRequest.builder()
                    .sourceBucket(bucket)
                    .sourceKey(sourceKey)
                    .destinationBucket(bucket)
                    .destinationKey(destinationKey)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build()
            );

            // ACL 설정
            s3Client.putObjectAcl(PutObjectAclRequest.builder()
                    .bucket(bucket)
                    .key(destinationKey)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build()
            );

            // 원본 객체 삭제
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(sourceKey)
                    .build()
            );

            // 이동된 파일의 새로운 URL 반환
            return "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + destinationKey;
        } catch (NoSuchKeyException e) {
            System.out.println("[ERROR] 이동한 파일이 S3에 존재하지 않습니다: " + destinationKey);
            throw new CustomException(ErrorCode.S3_FILE_NOT_FOUND);
        } catch (Exception e) {
            System.out.println("[ERROR] S3 파일 이동 중 오류 발생: " + e.getMessage());
            throw new CustomException(ErrorCode.S3_FILE_MOVE_FAILED);
        }
    }

    // S3 URL에서 버킷 이름 추출하는 메서드
    private String extractBucketName(String fileUrl) {
        String[] parts = fileUrl.split("/");
        return parts[2].split("\\.")[0]; // ex: seoul-milk-bucket.s3.ap-northeast-2.amazonaws.com → seoul-milk-bucket
    }

    // S3 URL에서 파일 키 추출하는 메서드
    private String extractFileKey(String fileUrl) {
        return fileUrl.substring(fileUrl.indexOf(".com/") + 5);
    }
}
