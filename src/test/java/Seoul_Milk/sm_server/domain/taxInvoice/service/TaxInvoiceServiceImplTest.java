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

    private static final String TEST_ISSUE_ID = "12345678-12345678-12345678";
    private static final MultipartFile MOCK_FILE = new MockMultipartFile("file", "test-image.png", "image/png", "content".getBytes());


    @BeforeEach
    void setUp() {
        testContainer = new TestContainer();
        testMember = createTestMember();

        testContainer.memberRepository.save(testMember);
        SecurityTestUtil.setAuthentication(testMember);

        // null이 들어가면 인자 불일치로 Mokito 호출 불가
        ReflectionTestUtils.setField(testContainer.taxInvoiceService, "clovaSecretKey", "test-secret-key");

        mockAwsS3Service();
        mockClovaOcrApi();

    }

    private MemberEntity createTestMember() {
        return MemberEntity.builder()
                .employeeId("11111111")
                .name("김재관")
                .password(new BCryptPasswordEncoder().encode("1234"))
                .role(Role.ROLE_ADMIN)
                .build();
    }

    private void mockAwsS3Service() {
        when(testContainer.awsS3Service.uploadFiles(anyString(), anyList(), anyBoolean()))
                .thenReturn(List.of("https://s3.aws.com/test-file.png"));

        when(testContainer.awsS3Service.uploadFile(anyString(), any(), anyBoolean()))
                .thenReturn("https://s3.aws.com/test-image.png");

        when(testContainer.awsS3Service.downloadFileFromS3(anyString()))
                .thenReturn(MOCK_FILE);
    }

    private void mockClovaOcrApi() {
        when(testContainer.clovaOcrApi.callApi(anyString(), any(), anyString(), anyString()))
                .thenReturn("{\"images\":[{\"fields\":[{\"name\":\"issueId\",\"inferText\":\"" + TEST_ISSUE_ID + "\"}]}]}");
    }

    @Test
    @DisplayName("OCR 처리 성공 - MultipartFile")
    void processTemplateOcrAsync_Success() throws Exception {
        // When
        CompletableFuture<TaxInvoiceResponseDTO.Create> response =
                testContainer.taxInvoiceService.processTemplateOcrAsync(MOCK_FILE, testMember);

        // Then
        assertThat(response).isNotNull();
        TaxInvoiceResponseDTO.Create result = response.get();
        assertThat(result.fileName()).isEqualTo("test-image.png");
        assertThat(result.extractedData()).isNotNull();
        assertThat(result.extractedData().get("issueId"))
                .isEqualTo(TEST_ISSUE_ID);
        assertThat(result.processStatus()).isEqualTo("UNAPPROVED"); // issueId만 넣었기 때문
    }

    @Test
    @DisplayName("OCR 처리 성공 - 임시저장된 이미지 URL")
    void processTemplateOcrSyncFromUrl_Success() throws Exception {
        // Given
        testContainer.imageService.markAsTemporary("[]", List.of(MOCK_FILE), testMember);

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
        TaxInvoice savedInvoice = createTestTaxInvoice();
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
        TaxInvoice savedInvoice = createTestTaxInvoice();
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

    private TaxInvoice createTestTaxInvoice() {
        TaxInvoice invoice = TaxInvoice.builder()
                .taxInvoiceId(1L)
                .issueId(TEST_ISSUE_ID)
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
                .taxInvoice(invoice)
                .build();

        invoice.attachFile(taxFile);
        return invoice;
    }
}