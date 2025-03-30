package Seoul_Milk.sm_server.domain.taxInvoice.controller;

import Seoul_Milk.sm_server.domain.image.entity.Image;
import Seoul_Milk.sm_server.domain.member.entity.MemberEntity;
import Seoul_Milk.sm_server.domain.member.enums.Role;
import Seoul_Milk.sm_server.domain.taxInvoice.dto.TaxInvoiceResponseDTO;
import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import Seoul_Milk.sm_server.domain.taxInvoice.enums.ArapType;
import Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus;
import Seoul_Milk.sm_server.domain.taxInvoiceFile.entity.TaxInvoiceFile;
import Seoul_Milk.sm_server.global.common.exception.CustomException;
import Seoul_Milk.sm_server.global.common.exception.ErrorCode;
import Seoul_Milk.sm_server.global.common.response.SuccessResponse;
import Seoul_Milk.sm_server.mock.container.TestContainer;
import Seoul_Milk.sm_server.util.SecurityTestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class TaxInvoiceControllerTest {

    private TestContainer testContainer;
    private MemberEntity testMember;

    private static final String TEST_ISSUE_ID_1 = "12345678-12345678-11111111";
    private static final String TEST_ISSUE_ID_2 = "12345678-12345678-22222222";
    private static final String TEST_ISSUE_ID_3 = "12345678-12345678-33333333";

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

        TaxInvoice taxInvoice1 = createTaxInvoice(TEST_ISSUE_ID_1, "공급자1", "공급받는자1", ArapType.SALES, ProcessStatus.APPROVED, "2025-03-25");
        TaxInvoice taxInvoice2 = createTaxInvoice(TEST_ISSUE_ID_2, "공급자2", "공급받는자2", ArapType.SALES, ProcessStatus.PENDING, "2025-03-26");
        TaxInvoice taxInvoice3 = createTaxInvoice(TEST_ISSUE_ID_3, "공급자3", "공급받는자3", ArapType.SALES, ProcessStatus.UNAPPROVED, "2024-12-31");

        testContainer.taxInvoiceRepository.saveAll(List.of(taxInvoice1, taxInvoice2, taxInvoice3));

        when(testContainer.awsS3Service.downloadFileFromS3(anyString()))
                .thenReturn(new MockMultipartFile("file", "test.png", "image/png", "content".getBytes()));

        when(testContainer.clovaOcrApi.callApi(anyString(), any(), anyString(), anyString()))
                .thenReturn("{\"images\":[{\"fields\":[{\"name\":\"issueId\",\"inferText\":\"" + TEST_ISSUE_ID_1 + "\"}]}]}");

        ReflectionTestUtils.setField(testContainer.taxInvoiceService, "clovaSecretKey", "test-secret-key");
    }


    @Test
    @DisplayName("OCR 처리 성공 - 로컬 업로드 + 임시 저장 이미지")
    void processParallelMultipleImages_Success() throws JsonProcessingException {
        // Given
        MultipartFile file = new MockMultipartFile("file", "upload.png", "image/png", "content".getBytes());

        when(testContainer.awsS3Service.uploadFiles(anyString(), anyList(), anyBoolean()))
                .thenReturn(List.of("https://s3.aws.com/test-image.png"));
        testContainer.imageService.markAsTemporary("[]", List.of(file), testMember);

        Page<Image> savedImages = testContainer.imageRepository.searchTempImages(testMember, PageRequest.of(0, 10));
        List<Long> imageIds = savedImages.stream().map(Image::getId).toList();

        // When
        SuccessResponse<List<TaxInvoiceResponseDTO.Create>> response =
                testContainer.taxInvoiceController.processParallelMultipleImages(List.of(file), imageIds, testMember);

        // Then
        Map<String, TaxInvoiceResponseDTO.Create> resultMap = response.getResult().stream()
                .collect(Collectors.toMap(TaxInvoiceResponseDTO.Create::fileName, r -> r));

        assertThat(resultMap.get("upload.png")).isNotNull();
        assertThat(resultMap.get("upload.png").extractedData().get("issueId")).isEqualTo(TEST_ISSUE_ID_1);
        assertThat(resultMap.get("upload.png").processStatus()).isEqualTo("UNAPPROVED");

        assertThat(resultMap.get("https://s3.aws.com/test-image.png")).isNotNull();
        assertThat(resultMap.get("https://s3.aws.com/test-image.png").extractedData().get("issueId")).isEqualTo(TEST_ISSUE_ID_1);
        assertThat(resultMap.get("https://s3.aws.com/test-image.png").processStatus()).isEqualTo("UNAPPROVED");

    }

    @Test
    @DisplayName("OCR 처리 실패 - 로컬 이미지와 임시 이미지 모두 없는 경우")
    void processParallelMultipleImages_Fail_NoImages() {
        // When
        // Then
        assertThatThrownBy(() ->
                testContainer.taxInvoiceController.processParallelMultipleImages(null, null, testMember)
        ).isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.UPLOAD_FAILED.getMessage());
    }

    @Test
    @DisplayName("세금계산서 삭제 성공")
    void deleteTaxInvoice_Success() {
        // Given
        TaxInvoice invoice = createTaxInvoice(
                "00000000-00000000-00000000", "삭제용 공급자", "삭제용 공급받는자",
                ArapType.SALES, ProcessStatus.APPROVED, "2025-04-01"
        );
        TaxInvoice savedInvoice = testContainer.taxInvoiceRepository.save(invoice);

        // When
        SuccessResponse<String> response = testContainer.taxInvoiceController.delete(savedInvoice.getTaxInvoiceId());

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getResult()).isEqualTo("세금계산서 정보 삭제에 성공했습니다.");
        assertThat(testContainer.taxInvoiceRepository.findById(invoice.getTaxInvoiceId())).isEmpty();
    }

    @Test
    @DisplayName("세금계산서 삭제 실패 - 존재하지 않는 ID")
    void deleteTaxInvoice_Fail_NotFound() {
        // When
        // Then
        assertThatThrownBy(() ->
                testContainer.taxInvoiceController.delete(999L)
        ).isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.TAX_INVOICE_NOT_EXIST.getMessage());
    }


    @Test
    @DisplayName("세금계산서 검색 성공 - 공급자, 공급받는자 필터 테스트")
    void search_By_Provider_Filter() {
        // When
        SuccessResponse<Page<TaxInvoiceResponseDTO.GetOne>> response = testContainer.taxInvoiceController.getAllBySearch(
                testMember, "공급자1", "공급받는자1", null,
                null, null, null, null, 1, 10
        );

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getResult().getContent()).hasSize(1);
        assertThat(response.getResult().getContent().get(0).issueId()).isEqualTo(TEST_ISSUE_ID_1);
        assertThat(response.getResult().getContent().get(0).status()).isEqualTo("APPROVED");
    }

    @Test
    @DisplayName("세금계산서 검색 성공 - 미승인 상태 필터")
    void search_By_Status_Filter() {
        // When
        SuccessResponse<Page<TaxInvoiceResponseDTO.GetOne>> response = testContainer.taxInvoiceController.getAllBySearch(
                testMember, null, null, null,
                null, null, null, "UNAPPROVED", 1, 10
        );

        // Then
        assertThat(response.getResult().getContent()).hasSize(1);
        assertThat(response.getResult().getContent().get(0).issueId()).isEqualTo(TEST_ISSUE_ID_3);
        assertThat(response.getResult().getContent().get(0).status()).isEqualTo("UNAPPROVED");
    }

    @Test
    @DisplayName("세금계산서 검색 성공 - 발행일 필터")
    void search_By_Date_Filter() {
        // When
        SuccessResponse<Page<TaxInvoiceResponseDTO.GetOne>> response =
                testContainer.taxInvoiceController.getAllBySearch(
                        testMember,
                        null, null, null,
                        LocalDate.of(2025, 3, 25),
                        LocalDate.of(2025, 3, 26),
                        null, null, 1, 10
                );

        // Then
        assertThat(response.getResult().getContent()).hasSize(2);
        List<String> issueIds = response.getResult().getContent().stream()
                .map(TaxInvoiceResponseDTO.GetOne::issueId)
                .toList();

        assertThat(issueIds).containsExactlyInAnyOrder(TEST_ISSUE_ID_1, TEST_ISSUE_ID_2);
    }

    @Test
    @DisplayName("엑셀 추출 성공")
    void extractToExcel_Success() throws IOException {
        // Given
        byte[] excelData = new byte[]{1, 2, 3};
        when(testContainer.excelMaker.getTaxInvoiceToExcel(anyList()))
                .thenReturn(new ByteArrayInputStream(excelData));

        // When
        ResponseEntity<InputStreamResource> response = testContainer.taxInvoiceController.toExcel(List.of(1L, 2L, 3L));

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getBody().contentLength()).isEqualTo(3);
        assertThat(response.getHeaders().getContentType().toString()).isEqualTo("application/vnd.ms-excel");
    }

    private TaxInvoice createTaxInvoice(
            String issueId,
            String provider,
            String consumer,
            ArapType arap,
            ProcessStatus status,
            String erDat
    ) {
        TaxInvoice invoice = TaxInvoice.builder()
                .issueId(issueId)
                .arap(arap)
                .processStatus(status)
                .ipId("111-11-11111")
                .suId("222-22-22222")
                .ipBusinessName(provider)
                .suBusinessName(consumer)
                .erDat(erDat)
                .chargeTotal(3000)
                .member(testMember)
                .createAt(LocalDate.parse(erDat).atStartOfDay()) // 테스트 용이성을 위해
                .build();

        TaxInvoiceFile taxFile = TaxInvoiceFile.builder()
                .fileUrl("https://s3.aws.com/" + issueId + ".png")
                .taxInvoice(invoice)
                .build();

        invoice.attachFile(taxFile);
        return invoice;
    }

}