package Seoul_Milk.sm_server.domain.taxInvoice.service;

import static Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus.APPROVED;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus.REJECTED;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus.UNAPPROVED;
import static Seoul_Milk.sm_server.global.common.exception.ErrorCode.DO_NOT_ACCESS_OTHER_TAX_INVOICE;
import static Seoul_Milk.sm_server.global.common.exception.ErrorCode.TAX_INVOICE_NOT_EXIST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import Seoul_Milk.sm_server.domain.member.entity.MemberEntity;
import Seoul_Milk.sm_server.domain.member.enums.Role;
import Seoul_Milk.sm_server.domain.taxInvoice.dto.history.TaxInvoiceHistoryRequestDTO.TaxInvoiceRequest;
import Seoul_Milk.sm_server.domain.taxInvoice.dto.history.TaxInvoiceHistoryResponseDTO.GetHistoryData;
import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import Seoul_Milk.sm_server.domain.taxInvoice.enums.ArapType;
import Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus;
import Seoul_Milk.sm_server.domain.taxInvoice.validator.TaxInvoiceValidator;
import Seoul_Milk.sm_server.domain.taxInvoiceFile.entity.TaxInvoiceFile;
import Seoul_Milk.sm_server.global.common.exception.CustomException;
import Seoul_Milk.sm_server.mock.repository.FakeTaxInvoiceFileRepository;
import Seoul_Milk.sm_server.mock.repository.FakeTaxInvoiceRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class TaxInvoiceHistoryServiceImplTest {
    private FakeTaxInvoiceRepository taxInvoiceRepository;
    private FakeTaxInvoiceFileRepository taxInvoiceFileRepository;
    private TaxInvoiceValidator taxInvoiceValidator;
    private TaxInvoiceHistoryServiceImpl taxInvoiceHistoryService;
    private MemberEntity testMember;

    @BeforeEach
    void setUp(){
        taxInvoiceRepository = new FakeTaxInvoiceRepository();
        taxInvoiceFileRepository = new FakeTaxInvoiceFileRepository();
        taxInvoiceValidator = new TaxInvoiceValidator();
        taxInvoiceHistoryService = TaxInvoiceHistoryServiceImpl.builder()
                .taxInvoiceValidator(taxInvoiceValidator)
                .taxInvoiceRepository(taxInvoiceRepository)
                .build();
        testMember = MemberEntity.builder()
                .id(1L)
                .name("김영록")
                .email("praoo800@naver.com")
                .employeeId("202011269")
                .password(new BCryptPasswordEncoder().encode("1234"))
                .role(Role.ROLE_ADMIN)
                .build();
    }

    @Test
    @DisplayName("상태별로 개수 잘 세는지 테스트")
    void countTest(){
        //Given
        List<TaxInvoice> invoices = List.of(
                createTaxInvoice(1L, "1", UNAPPROVED, "1", "1", "2024-03-26", null, null),
                createTaxInvoice(2L, "2", APPROVED, "2", "2", "2024-03-26", null, null),
                createTaxInvoice(3L, "3", REJECTED, "3", "3", "2024-03-26", null, null)
        );

        for (int i = 0; i < invoices.size(); i++) {
            TaxInvoice invoice = invoices.get(i);
            TaxInvoiceFile file = createTaxInvoiceFile((long) (i + 1), invoice);
            taxInvoiceRepository.save(invoice);
            taxInvoiceFileRepository.save(file);
        }

        //when
        Long total = taxInvoiceHistoryService.searchByProviderOrConsumer(testMember, null, "", 0, 10).total();
        Long approve = taxInvoiceHistoryService.searchByProviderOrConsumer(testMember, APPROVED, "", 0, 10).approved();
        Long rejected = taxInvoiceHistoryService.searchByProviderOrConsumer(testMember, REJECTED, "", 0, 10).rejected();
        Long unapproved = taxInvoiceHistoryService.searchByProviderOrConsumer(testMember, UNAPPROVED, "", 0, 10).unapproved();

        //then
        Assertions.assertEquals(total, 3);
        Assertions.assertEquals(approve, 1);
        Assertions.assertEquals(rejected, 1);
        Assertions.assertEquals(unapproved, 1);
    }

    @Test
    @DisplayName("검색어 테스트 : 공급자 또는 공급받는자 검색했을 때")
    void pocSearchTest(){
        //Given
        TaxInvoice correctTaxInvoice1 = createTaxInvoice(1L, "1", UNAPPROVED, "1", "1", "2024-03-26", null, "서울우유 1");
        TaxInvoice correctTaxInvoice2 = createTaxInvoice(3L, "3", APPROVED, "3", "3", "2024-03-26", "서울우유 2", null);
        TaxInvoice incorrectTaxInvoice1 = createTaxInvoice(2L, "2", APPROVED, "2", "2", "2024-03-26", "부산마트", null);
        List<TaxInvoice> invoices = List.of(
                correctTaxInvoice1,
                correctTaxInvoice2,
                incorrectTaxInvoice1
        );
        for (int i = 0; i < invoices.size(); i++) {
            TaxInvoice invoice = invoices.get(i);
            TaxInvoiceFile file = createTaxInvoiceFile((long) (i + 1), invoice);
            taxInvoiceRepository.save(invoice);
            taxInvoiceFileRepository.save(file);
        }

        //when
        List<GetHistoryData> historyDataList = taxInvoiceHistoryService.searchByProviderOrConsumer(testMember, null, "서울우유", 0, 10)
                .page().getContent();
        // Then
        assertThat(historyDataList).hasSize(2);

        /**
         * 구현할때 매핑을 반대로 해서
         * assertThat(data.suBusinessName()).isEqualTo(correctTaxInvoice1.getIpBusinessName());
         * assertThat(data.ipBusinessName()).isEqualTo(correctTaxInvoice1.getSuBusinessName());
         * 이렇게 함
         */
        List<GetHistoryData> sortedList = new ArrayList<>(historyDataList);
        sortedList.sort(Comparator.comparing(GetHistoryData::id));
        GetHistoryData data1 = sortedList.get(0);
        assertThat(data1.suBusinessName()).isEqualTo(correctTaxInvoice1.getIpBusinessName());
        assertThat(data1.ipBusinessName()).isEqualTo(correctTaxInvoice1.getSuBusinessName());
        assertThat(data1.createdAt()).isEqualTo(correctTaxInvoice1.getCreateAt());
        assertThat(data1.processStatus()).isEqualTo(correctTaxInvoice1.getProcessStatus());
        assertThat(data1.url()).isEqualTo(correctTaxInvoice1.getFile().getFileUrl());

        GetHistoryData data2 = sortedList.get(1);
        assertThat(data2.suBusinessName()).isEqualTo(correctTaxInvoice2.getIpBusinessName());
        assertThat(data2.ipBusinessName()).isEqualTo(correctTaxInvoice2.getSuBusinessName());
        assertThat(data2.createdAt()).isEqualTo(correctTaxInvoice2.getCreateAt());
        assertThat(data2.processStatus()).isEqualTo(correctTaxInvoice2.getProcessStatus());
        assertThat(data2.url()).isEqualTo(correctTaxInvoice2.getFile().getFileUrl());
    }

    @Test
    @DisplayName("공급자 공급받는자 그리고 승인상태별 검색 시")
    void pocAndOptionSearchTest(){
        TaxInvoice correctTaxInvoice1 = createTaxInvoice(1L, "1", APPROVED, "1", "1", "2024-03-26", null, "서울우유 1");
        TaxInvoice incorrectTaxInvoice1 = createTaxInvoice(3L, "3", UNAPPROVED, "3", "3", "2024-03-26", "서울우유 2", null);
        TaxInvoice incorrectTaxInvoice2 = createTaxInvoice(2L, "2", APPROVED, "2", "2", "2024-03-26", "부산마트", null);
        List<TaxInvoice> invoices = List.of(
                correctTaxInvoice1,
                incorrectTaxInvoice1,
                incorrectTaxInvoice2
        );
        for (int i = 0; i < invoices.size(); i++) {
            TaxInvoice invoice = invoices.get(i);
            TaxInvoiceFile file = createTaxInvoiceFile((long) (i + 1), invoice);
            taxInvoiceRepository.save(invoice);
            taxInvoiceFileRepository.save(file);
        }
        // when
        List<GetHistoryData> historyDataList = taxInvoiceHistoryService.searchByProviderOrConsumer(testMember, APPROVED, "서울우유", 0, 10)
                .page().getContent();
        // Then
        assertThat(historyDataList).hasSize(1);

        GetHistoryData data1 = historyDataList.get(0);
        assertThat(data1.suBusinessName()).isEqualTo(correctTaxInvoice1.getIpBusinessName());
        assertThat(data1.ipBusinessName()).isEqualTo(correctTaxInvoice1.getSuBusinessName());
        assertThat(data1.createdAt()).isEqualTo(correctTaxInvoice1.getCreateAt());
        assertThat(data1.processStatus()).isEqualTo(correctTaxInvoice1.getProcessStatus());
        assertThat(data1.url()).isEqualTo(correctTaxInvoice1.getFile().getFileUrl());
    }

    @Test
    @DisplayName("검증데이터 삭제 테스트")
    void deleteTaxInvoiceTest(){
        //given
        TaxInvoice taxInvoice1 = createTaxInvoice(1L, "1", APPROVED, "1", "1", "2024-03-26", null, "서울우유 1");
        TaxInvoice taxInvoice2 = createTaxInvoice(2L, "2", APPROVED, "2", "2", "2024-03-26", "부산마트", null);
        List<TaxInvoice> invoices = List.of(
                taxInvoice1,
                taxInvoice2
        );
        for (int i = 0; i < invoices.size(); i++) {
            TaxInvoice invoice = invoices.get(i);
            TaxInvoiceFile file = createTaxInvoiceFile((long) (i + 1), invoice);
            taxInvoiceRepository.save(invoice);
            taxInvoiceFileRepository.save(file);
        }
        TaxInvoiceRequest taxInvoiceRequest = TaxInvoiceRequest.builder()
                        .taxInvoiceIdList(List.of(1L))
                        .build();

        //when
        taxInvoiceHistoryService.deleteValidationTaxInvoice(testMember, taxInvoiceRequest);

        //then
        List<TaxInvoice> historyDataList = taxInvoiceRepository.findAll();
        assertThat(historyDataList).hasSize(1);

        TaxInvoice data1 = historyDataList.get(0);
        assertThat(data1.getTaxInvoiceId()).isEqualTo(2L);
        assertThat(data1.getIssueId()).isEqualTo("2");
        assertThat(data1.getProcessStatus()).isEqualTo(APPROVED);
        assertThat(data1.getIpId()).isEqualTo("2");
        assertThat(data1.getSuId()).isEqualTo("2");
        assertThat(data1.getErDat()).isEqualTo("2024-03-26");
        assertThat(data1.getIpBusinessName()).isEqualTo("부산마트");
        assertThat(data1.getSuBusinessName()).isEqualTo(null);
    }

    @Test
    @DisplayName("DB에 없는 검증데이터 삭제 시도 시 에러발생")
    void deleteNotExistValidationData(){
        TaxInvoice taxInvoice1 = createTaxInvoice(1L, "1", APPROVED, "1", "1", "2024-03-26", null, "서울우유 1");
        TaxInvoice taxInvoice2 = createTaxInvoice(2L, "2", APPROVED, "2", "2", "2024-03-26", "부산마트", null);
        List<TaxInvoice> invoices = List.of(
                taxInvoice1,
                taxInvoice2
        );
        for (int i = 0; i < invoices.size(); i++) {
            TaxInvoice invoice = invoices.get(i);
            TaxInvoiceFile file = createTaxInvoiceFile((long) (i + 1), invoice);
            taxInvoiceRepository.save(invoice);
            taxInvoiceFileRepository.save(file);
        }
        TaxInvoiceRequest taxInvoiceRequest = TaxInvoiceRequest.builder()
                .taxInvoiceIdList(List.of(3L, 4L))
                .build();

        assertThatThrownBy(() -> taxInvoiceHistoryService.deleteValidationTaxInvoice(testMember, taxInvoiceRequest))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(TAX_INVOICE_NOT_EXIST.getMessage());
    }

    @Test
    @DisplayName("본인것이 아닌 검증데이터 삭제 시 에러발생")
    void deleteNotMineValidationData(){
        TaxInvoice taxInvoice1 = createTaxInvoice(1L, "1", APPROVED, "1", "1", "2024-03-26", null, "서울우유 1");
        TaxInvoice taxInvoice2 = createTaxInvoice(2L, "2", APPROVED, "2", "2", "2024-03-26", "부산마트", null);
        List<TaxInvoice> invoices = List.of(
                taxInvoice1,
                taxInvoice2
        );
        for (int i = 0; i < invoices.size(); i++) {
            TaxInvoice invoice = invoices.get(i);
            TaxInvoiceFile file = createTaxInvoiceFile((long) (i + 1), invoice);
            taxInvoiceRepository.save(invoice);
            taxInvoiceFileRepository.save(file);
        }
        MemberEntity otherMember = MemberEntity.builder()
                .id(2L)
                .name("김영룩")
                .email("praoo900@naver.com")
                .employeeId("202011270")
                .password(new BCryptPasswordEncoder().encode("1234"))
                .role(Role.ROLE_NORMAL)
                .build();

        TaxInvoiceRequest taxInvoiceRequest = TaxInvoiceRequest.builder()
                .taxInvoiceIdList(List.of(1L))
                .build();

        assertThatThrownBy(() -> taxInvoiceHistoryService.deleteValidationTaxInvoice(otherMember, taxInvoiceRequest))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(DO_NOT_ACCESS_OTHER_TAX_INVOICE.getMessage());
    }

    private TaxInvoice createTaxInvoice(Long id, String issueId, ProcessStatus status, String ipId, String suId, String erDat, String ipBusinessName, String suBusinessName) {
        return TaxInvoice.builder()
                .taxInvoiceId(id)
                .issueId(issueId)
                .arap(ArapType.SALES)
                .processStatus(status)
                .ipId(ipId)
                .suId(suId)
                .chargeTotal(1)
                .erDat(erDat)
                .ipBusinessName(ipBusinessName)
                .suBusinessName(suBusinessName)
                .member(testMember)
                .createAt(LocalDateTime.now())
                .build();
    }

    private TaxInvoiceFile createTaxInvoiceFile(Long id, TaxInvoice invoice) {
        TaxInvoiceFile file = TaxInvoiceFile.builder()
                .id(id)
                .taxInvoice(invoice)
                .build();
        invoice.attachFile(file);
        return file;
    }

}
