package Seoul_Milk.sm_server.domain.taxInvoice.service;

import Seoul_Milk.sm_server.domain.member.entity.MemberEntity;
import Seoul_Milk.sm_server.domain.member.enums.Role;
import Seoul_Milk.sm_server.domain.taxInvoice.dto.TaxInvoiceResponseDTO;
import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import Seoul_Milk.sm_server.domain.taxInvoice.enums.ArapType;
import Seoul_Milk.sm_server.domain.taxInvoiceFile.entity.TaxInvoiceFile;
import Seoul_Milk.sm_server.global.common.exception.CustomException;
import Seoul_Milk.sm_server.global.common.exception.ErrorCode;
import Seoul_Milk.sm_server.mock.container.TestContainer;
import Seoul_Milk.sm_server.util.SecurityTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TaxInvoiceServiceImplTest {

    private TestContainer testContainer;
    private MemberEntity testMember;

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

        when(testContainer.awsS3Service.uploadFile(anyString(), any(), anyBoolean()))
                .thenReturn("https://s3.aws.com/test-file.png");

        when(testContainer.awsS3Service.downloadFileFromS3(anyString()))
                .thenReturn(new MockMultipartFile("file", "test.png", "image/png", "content".getBytes()));

        // null이 들어가면 인자 불일치로 Mokito 호출 불가
        ReflectionTestUtils.setField(testContainer.taxInvoiceService, "clovaSecretKey", "test-secret-key");

        when(testContainer.clovaOcrApi.callApi(
                anyString(),
                any(),
                anyString(),
                anyString()
        )).thenReturn("{\"images\":[{\"fields\":[{\"name\":\"issueId\",\"inferText\":\"12345678-12345678-12345678\"}]}]}");

    }


    @Test
    @DisplayName("OCR 처리 성공 - MultipartFile")
    void processTemplateOcrAsync_Success() throws Exception {
        // Given
        MultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "content".getBytes());

        // When
        CompletableFuture<TaxInvoiceResponseDTO.Create> response = testContainer.taxInvoiceService.processTemplateOcrAsync(file, testMember);

        // Then
        assertThat(response).isNotNull();
        TaxInvoiceResponseDTO.Create result = response.get();
        assertThat(result.fileName()).isEqualTo("test.png");
        assertThat(result.extractedData()).isNotNull();
        assertThat(result.extractedData().get("issueId"))
                .isEqualTo("12345678-12345678-12345678");
        assertThat(result.processStatus()).isEqualTo("UNAPPROVED"); // issueId만 넣었기 때문
    }

    @Test
    @DisplayName("OCR 처리 성공 - 임시저장된 이미지 URL")
    void processTemplateOcrSyncFromUrl_Success() throws Exception {
        // Given
        MultipartFile mockFile = new MockMultipartFile("file", "test-image.png", "image/png", "content".getBytes());

        when(testContainer.awsS3Service.uploadFiles(anyString(), anyList(), anyBoolean()))
                .thenReturn(List.of("https://s3.aws.com/test-image.png"));

        testContainer.imageService.markAsTemporary("[]", List.of(mockFile), testMember);

        var savedImages = testContainer.imageRepository.searchTempImages(testMember, PageRequest.of(0, 10));
        String imageUrl = savedImages.getContent().get(0).getImageUrl();
        Long imageId = savedImages.getContent().get(0).getId();

        // When
        CompletableFuture<TaxInvoiceResponseDTO.Create> response = testContainer.taxInvoiceService.processTemplateOcrSync(imageUrl, testMember, imageId);

        // Then
        assertThat(response).isNotNull();
        TaxInvoiceResponseDTO.Create result = response.get();
        assertThat(result.fileName()).isEqualTo(imageUrl);
        assertThat(result.extractedData()).isNotNull();
        assertThat(result.extractedData().get("issueId"))
                .isEqualTo("12345678-12345678-12345678");
        assertThat(result.processStatus()).isEqualTo("UNAPPROVED");
    }

    @Test
    @DisplayName("세금계산서 정보 삭제 성공")
    void deleteTaxInvoiceSuccessfully() {
        // Given
        TaxInvoice savedInvoice = TaxInvoice.builder()
                .taxInvoiceId(1L)
                .issueId("12345678-12345678-12345678")
                .arap(ArapType.SALES)
                .processStatus(PENDING)
                .ipId("123-12-12345")
                .suId("321-21-54321")
                .chargeTotal(3000)
                .erDat("2025-03-28")
                .ipBusinessName("공급자 사업체")
                .suBusinessName("공급받는자 사업체")
                .member(testMember)
                .createAt(LocalDateTime.now())
                .build();

        TaxInvoiceFile taxFile = TaxInvoiceFile.builder()
                .fileUrl("https://s3.aws.com/test-file.png")
                .taxInvoice(savedInvoice)
                .build();

        savedInvoice.attachFile(taxFile);
        testContainer.taxInvoiceRepository.save(savedInvoice);

        // When
        testContainer.taxInvoiceService.delete(savedInvoice.getTaxInvoiceId());

        //
        assertThat(testContainer.taxInvoiceRepository.findById(savedInvoice.getTaxInvoiceId())).isEmpty();
        verify(testContainer.awsS3Service, times(1)).deleteFile("https://s3.aws.com/test-file.png");
    }

    @Test
    @DisplayName("세금계산서 검색 성공")
    void searchTaxInvoicesSuccessfully() {
        // Given
        LocalDate now = LocalDate.now();
        TaxInvoice savedInvoice = TaxInvoice.builder()
                .taxInvoiceId(1L)
                .issueId("12345678-12345678-12345678")
                .arap(ArapType.SALES)
                .processStatus(PENDING)
                .ipId("123-12-12345")
                .suId("321-21-54321")
                .chargeTotal(3000)
                .erDat("2025-03-28")
                .ipBusinessName("공급자 사업체")
                .suBusinessName("공급받는자 사업체")
                .member(testMember)
                .createAt(LocalDateTime.now())
                .build();

        TaxInvoiceFile taxFile = TaxInvoiceFile.builder()
                .fileUrl("https://s3.aws.com/test-file.png")
                .taxInvoice(savedInvoice)
                .build();

        savedInvoice.attachFile(taxFile);
        testContainer.taxInvoiceRepository.save(savedInvoice);

        // When
        Page<TaxInvoiceResponseDTO.GetOne> result = testContainer.taxInvoiceService.search(
                testMember, "공급자 사업체", "공급받는자 사업체", null, now.minusDays(1), now.plusDays(1), null, null, 0, 10
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).issueId()).isEqualTo("12345678-12345678-12345678");
    }

    @Test
    @DisplayName("엑셀 파일 추출 성공")
    void extractToExcelSuccessfully() throws IOException {
        // Given
        byte[] excelData = new byte[]{1, 2, 3};

        when(testContainer.excelMaker.getTaxInvoiceToExcel(anyList()))
                .thenReturn(new ByteArrayInputStream(excelData));

        // When
        var result = testContainer.taxInvoiceService.extractToExcel(List.of(1L, 2L, 3L));

        // Then
        assertThat(result).isNotNull();
        verify(testContainer.excelMaker, times(1)).getTaxInvoiceToExcel(anyList());
    }

    @Test
    @DisplayName("엑셀 파일 추출 실패 - 데이터 없음")
    void extractToExcelFail_NoData() throws IOException {
        // When
        when(testContainer.excelMaker.getTaxInvoiceToExcel(anyList()))
                .thenThrow(new CustomException(ErrorCode.MAKE_EXCEL_FILE_ERROR));

        // Then
        assertThatThrownBy(() -> testContainer.taxInvoiceService.extractToExcel(List.of()))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.MAKE_EXCEL_FILE_ERROR.getMessage());
    }
}