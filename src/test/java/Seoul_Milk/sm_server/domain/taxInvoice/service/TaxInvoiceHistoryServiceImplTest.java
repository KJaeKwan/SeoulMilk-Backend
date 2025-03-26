package Seoul_Milk.sm_server.domain.taxInvoice.service;

import static Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus.APPROVED;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus.REJECTED;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus.UNAPPROVED;
import static org.assertj.core.api.Assertions.assertThat;

import Seoul_Milk.sm_server.domain.member.entity.MemberEntity;
import Seoul_Milk.sm_server.domain.member.enums.Role;
import Seoul_Milk.sm_server.domain.taxInvoice.dto.history.TaxInvoiceHistoryResponseDTO.GetHistoryData;
import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import Seoul_Milk.sm_server.domain.taxInvoice.enums.ArapType;
import Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus;
import Seoul_Milk.sm_server.domain.taxInvoice.validator.TaxInvoiceValidator;
import Seoul_Milk.sm_server.domain.taxInvoiceFile.entity.TaxInvoiceFile;
import Seoul_Milk.sm_server.mock.repository.FakeTaxInvoiceFileRepository;
import Seoul_Milk.sm_server.mock.repository.FakeTaxInvoiceRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaxInvoiceHistoryServiceImplTest {
    private FakeTaxInvoiceRepository taxInvoiceRepository;
    private FakeTaxInvoiceFileRepository taxInvoiceFileRepository;
    private TaxInvoiceValidator taxInvoiceValidator;
    private TaxInvoiceHistoryServiceImpl taxInvoiceHistoryService;
    private MemberEntity testMember;

    @BeforeEach
    void init(){
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
                .password("1234")
                .role(Role.ROLE_ADMIN)
                .build();
    }

    @Test
    void 상태별로_개수_잘_세는지_테스트(){
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
    void 검색어_테스트_공급자_또는_공급받는자_검색했을_때(){
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
        assertThat(historyDataList).anySatisfy(data -> {
            assertThat(data.suBusinessName()).isEqualTo(correctTaxInvoice1.getIpBusinessName());
            assertThat(data.ipBusinessName()).isEqualTo(correctTaxInvoice1.getSuBusinessName());
            assertThat(data.createdAt()).isEqualTo(correctTaxInvoice1.getCreateAt());
            assertThat(data.processStatus()).isEqualTo(correctTaxInvoice1.getProcessStatus());
            assertThat(data.url()).isEqualTo(correctTaxInvoice1.getFile().getFileUrl());
        });

        assertThat(historyDataList).anySatisfy(data -> {
            assertThat(data.suBusinessName()).isEqualTo(correctTaxInvoice2.getIpBusinessName());
            assertThat(data.ipBusinessName()).isEqualTo(correctTaxInvoice2.getSuBusinessName());
            assertThat(data.createdAt()).isEqualTo(correctTaxInvoice2.getCreateAt());
            assertThat(data.processStatus()).isEqualTo(correctTaxInvoice2.getProcessStatus());
            assertThat(data.url()).isEqualTo(correctTaxInvoice2.getFile().getFileUrl());
        });
    }

    @Test
    void 공급자_공급받는자_그리고_승인상태별_검색_시(){
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

        assertThat(historyDataList).anySatisfy(data -> {
            assertThat(data.suBusinessName()).isEqualTo(correctTaxInvoice1.getIpBusinessName());
            assertThat(data.ipBusinessName()).isEqualTo(correctTaxInvoice1.getSuBusinessName());
            assertThat(data.createdAt()).isEqualTo(correctTaxInvoice1.getCreateAt());
            assertThat(data.processStatus()).isEqualTo(correctTaxInvoice1.getProcessStatus());
            assertThat(data.url()).isEqualTo(correctTaxInvoice1.getFile().getFileUrl());
        });
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
