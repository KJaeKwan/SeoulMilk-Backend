package Seoul_Milk.sm_server.domain.image.controller;

import Seoul_Milk.sm_server.domain.image.dto.ImageResponseDTO;
import Seoul_Milk.sm_server.domain.image.entity.Image;
import Seoul_Milk.sm_server.domain.member.entity.MemberEntity;
import Seoul_Milk.sm_server.domain.member.enums.Role;
import Seoul_Milk.sm_server.global.common.exception.CustomException;
import Seoul_Milk.sm_server.global.common.exception.ErrorCode;
import Seoul_Milk.sm_server.global.common.response.SuccessResponse;
import Seoul_Milk.sm_server.mock.container.TestContainer;
import Seoul_Milk.sm_server.util.SecurityTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class ImageControllerTest {

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
                    List<MultipartFile> files = invocation.getArgument(1);
                    return files.stream()
                            .map(file -> "https://s3.aws.com/" + file.getOriginalFilename())
                            .toList();
                });
    }

    @Test
    @DisplayName("임시 저장된 이미지 전체 조회 성공")
    void getAllTemporaryImagesSuccessfully() throws Exception {
        // Given
        testContainer.imageService.markAsTemporary("[]", mockFiles, testMember);

        // When
        SuccessResponse<Page<ImageResponseDTO.GetOne>> response = testContainer.imageController.getAllTemporary(testMember, 1, 10);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getResult().getContent()).hasSize(2);
        assertThat(response.getResult().getContent().get(0).imageUrl()).contains("https://s3.aws.com/test-image1.png");
        assertThat(response.getResult().getContent().get(1).imageUrl()).contains("https://s3.aws.com/test-image2.png");
    }

    @Test
    @DisplayName("이미지 임시 저장 성공")
    void markAsTemporarySuccessfully() throws Exception {
        // When
        SuccessResponse<String> response = testContainer.imageController.markAsTemporary("[]", mockFiles, testMember);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getResult()).isEqualTo("선택한 이미지들이 임시 저장으로 설정되었습니다.");
    }

    @Test
    @DisplayName("임시 저장 이미지 삭제 성공")
    void removeFromTemporarySuccessfully() throws Exception {
        // Given
        testContainer.imageService.markAsTemporary("[]", mockFiles, testMember);

        var savedImages = testContainer.imageRepository.searchTempImages(testMember, PageRequest.of(0, 10));
        List<Long> imageIds = savedImages.stream().map(Image::getId).toList();

        // When
        SuccessResponse<String> response = testContainer.imageController.removeFromTemporary(testMember, imageIds);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getResult()).isEqualTo("모든 임시 저장 이미지가 해제되었습니다.");
        assertThat(testContainer.imageRepository.findByMemberAndIds(testMember, imageIds)).isEmpty();
    }

    @Test
    @DisplayName("임시 저장 이미지 삭제 실패 - 잘못된 이미지 ID")
    void removeFromTemporaryFailByInvalidId() {
        // Given
        List<Long> nonExistentIds = List.of();

        // When
        // Then
        assertThatThrownBy(() -> testContainer.imageController.removeFromTemporary(testMember, nonExistentIds))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.TMP_IMAGE_NOT_EXIST.getMessage());
    }

    @Test
    @DisplayName("임시 저장 이미지 삭제 실패 - 본인이 아닌 사용자")
    void removeFromTemporaryFailByWrongUser() throws Exception {
        // Given
        testContainer.imageService.markAsTemporary("[]", mockFiles, testMember);

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
        assertThatThrownBy(() -> testContainer.imageController.removeFromTemporary(anotherUser, imageIds))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.UNAUTHORIZED_ACCESS.getMessage());
    }

}