package Seoul_Milk.sm_server.domain.image.service;

import Seoul_Milk.sm_server.domain.image.entity.Image;
import Seoul_Milk.sm_server.domain.member.entity.MemberEntity;
import Seoul_Milk.sm_server.domain.member.enums.Role;
import Seoul_Milk.sm_server.global.common.exception.CustomException;
import Seoul_Milk.sm_server.global.common.exception.ErrorCode;
import Seoul_Milk.sm_server.mock.container.TestContainer;
import Seoul_Milk.sm_server.util.SecurityTestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ImageServiceImplTest {

    private TestContainer testContainer;
    private MemberEntity testMember;
    private List<MultipartFile> mockFiles;

    @BeforeEach
    void setUp() {
        testContainer = new TestContainer();

        testMember = MemberEntity.builder()
                .employeeId("11111111")
                .name("김재관")
                .password(new BCryptPasswordEncoder().encode("1234"))
                .role(Role.ROLE_ADMIN)
                .build();

        testContainer.memberRepository.save(testMember);
        SecurityTestUtil.setAuthentication(testMember);

        mockFiles = List.of(
                new MockMultipartFile("files", "test-image1.png", "image/png", "content1".getBytes()),
                new MockMultipartFile("files", "test-image2.png", "image/png", "content2".getBytes())
        );

        when(testContainer.awsS3Service.uploadFiles(anyString(), anyList(), anyBoolean()))
                .thenAnswer(invocation -> {
                    List<MockMultipartFile> files = invocation.getArgument(1);
                    return files.stream()
                            .map(file -> "https://s3.aws.com/" + file.getOriginalFilename())
                            .toList();
                });
    }

    @Test
    @DisplayName("이미지 임시 저장 성공")
    void markAsTemporarySuccessfully() throws JsonProcessingException {
        // Given
        String requestJson = "[]";

        // When
        testContainer.imageService.markAsTemporary(requestJson, mockFiles, testMember);
        var result = testContainer.imageRepository.searchTempImages(testMember, PageRequest.of(0, 10));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).getImageUrl()).contains("https://s3.aws.com/test-image1.png");
        assertThat(result.getContent().get(1).getImageUrl()).contains("https://s3.aws.com/test-image2.png");

        verify(testContainer.awsS3Service, times(1)).uploadFiles(eq("temporary-images"), anyList(), eq(true));
    }

    @Test
    @DisplayName("임시 저장된 이미지 전체 조회 성공")
    void getAllTempImagesSuccessfully() throws JsonProcessingException {
        // Given
        String requestJson = "[]";

        testContainer.imageService.markAsTemporary(requestJson, mockFiles, testMember);

        // When
        var result = testContainer.imageService.getAll(testMember, 0, 10);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).imageUrl()).contains("https://s3.aws.com/test-image1.png");
        assertThat(result.getContent().get(1).imageUrl()).contains("https://s3.aws.com/test-image2.png");
    }

    @Test
    @DisplayName("임시 저장 이미지 삭제 성공")
    void removeFromTemporarySuccessfully() throws JsonProcessingException {
        // Given
        String requestJson = "[]";
        testContainer.imageService.markAsTemporary(requestJson, mockFiles, testMember);

        var savedImages = testContainer.imageRepository.searchTempImages(testMember, PageRequest.of(0, 10));
        List<Long> imageIds = savedImages.stream().map(Image::getId).toList();

        // When
        testContainer.imageService.removeFromTemporary(testMember, imageIds);

        // Then
        assertThat(savedImages.getContent()).hasSize(2);
        assertThat(testContainer.imageRepository.findByMemberAndIds(testMember, imageIds)).isEmpty();
        for (Image image : savedImages) {
            verify(testContainer.awsS3Service, times(1)).deleteFile(image.getImageUrl());
        }
    }

    @Test
    @DisplayName("이미지 삭제 실패 - 이미지가 존재하지 않을 때")
    void removeFromTemporaryImageNotFound() {
        // When
        var savedImages = testContainer.imageRepository.searchTempImages(testMember, PageRequest.of(0, 10));
        List<Long> imageIds = savedImages.stream().map(Image::getId).toList();

        // Then
        assertThatThrownBy(() -> testContainer.imageService.removeFromTemporary(testMember, imageIds))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.TMP_IMAGE_NOT_EXIST.getMessage());
    }

    @Test
    @DisplayName("이미지 삭제 실패 - 본인의 이미지가 아닐 때")
    void removeFromTemporaryWithWrongUser() throws JsonProcessingException {
        // Given
        String requestJson = "[]";
        testContainer.imageService.markAsTemporary(requestJson, mockFiles, testMember);

        var savedImages = testContainer.imageRepository.searchTempImages(testMember, PageRequest.of(0, 10));
        List<Long> imageIds = savedImages.stream().map(Image::getId).toList();

        MemberEntity anotherUser = MemberEntity.builder()
                .employeeId("22222222")
                .name("김관재")
                .password(new BCryptPasswordEncoder().encode("1234"))
                .role(Role.ROLE_NORMAL)
                .build();

        testContainer.memberRepository.save(anotherUser);
        SecurityTestUtil.setAuthentication(anotherUser);

        // When
        // Then
        assertThatThrownBy(() -> testContainer.imageService.removeFromTemporary(anotherUser, imageIds))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.UNAUTHORIZED_ACCESS.getMessage());
    }
}