package Seoul_Milk.sm_server.domain.taxInvoice.controller;


import static Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus.UNAPPROVED;
import static org.assertj.core.api.Assertions.assertThat;

import Seoul_Milk.sm_server.domain.member.entity.MemberEntity;
import Seoul_Milk.sm_server.domain.member.enums.Role;
import Seoul_Milk.sm_server.domain.taxInvoice.dto.history.TaxInvoiceHistoryResponseDTO.GetHistoryData;
import Seoul_Milk.sm_server.domain.taxInvoice.dto.history.TaxInvoiceHistoryResponseDTO.TaxInvoiceSearchResult;
import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import Seoul_Milk.sm_server.domain.taxInvoice.enums.ArapType;
import Seoul_Milk.sm_server.domain.taxInvoiceFile.entity.TaxInvoiceFile;
import Seoul_Milk.sm_server.global.common.response.SuccessResponse;
import Seoul_Milk.sm_server.mock.container.TestContainer;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TaxInvoiceHistoryControllerTest {
    private TestContainer testContainer;
    private MemberEntity testMember;

    @BeforeEach
    void setUp(){
        testContainer = new TestContainer();

        testMember = MemberEntity.builder()
                .id(1L)
                .name("김영록")
                .email("praoo800@naver.com")
                .employeeId("202011269")
                .password(new BCryptPasswordEncoder().encode("1234"))
                .role(Role.ROLE_ADMIN)
                .build();
        testContainer.memberRepository.save(testMember);
    }

    @Test
    @DisplayName("<RE_01> 공급자 또는 공급받는자 검색 기능 + 승인, 반려, 검증실패별 조회 컨트롤러 테스트[매핑 제대로 되나 테스트]")
    void searchAndShowByFilterControllerTest(){
        TaxInvoice taxInvoice = TaxInvoice.builder()
                .taxInvoiceId(1L)
                .issueId("11111")
                .arap(ArapType.SALES)
                .processStatus(UNAPPROVED)
                .ipId("1111")
                .suId("1111")
                .chargeTotal(3000)
                .erDat("2025-03-27")
                .ipBusinessName("서울우유")
                .suBusinessName("부산우유")
                .member(testMember)
                .createAt(LocalDateTime.now())
                .build();
        TaxInvoiceFile file = TaxInvoiceFile.builder()
                .id(1L)
                .taxInvoice(taxInvoice)
                .fileUrl("urlurl")
                .build();
        taxInvoice.attachFile(file);
        testContainer.taxInvoiceFileRepository.save(file);
        testContainer.taxInvoiceRepository.save(taxInvoice);

        SuccessResponse<TaxInvoiceSearchResult> response =
                testContainer.taxInvoiceHistoryController
                        .searchAndShowByFilter(testMember, null, null, 1, 10);
        assertThat(response).isNotNull();

        GetHistoryData pageData = response.getResult().page().getContent().get(0);
        Long total = response.getResult().total();
        Long approved = response.getResult().approved();
        Long rejected = response.getResult().rejected();
        Long unapproved = response.getResult().unapproved();

        // 구현할때 매핑 반대로 해서 이렇게 코드짜짐
        assertThat(pageData.suBusinessName()).isEqualTo(taxInvoice.getIpBusinessName());
        assertThat(pageData.ipBusinessName()).isEqualTo(taxInvoice.getSuBusinessName());
        assertThat(pageData.url()).isEqualTo(taxInvoice.getFile().getFileUrl());
        assertThat(pageData.createdAt()).isEqualTo(taxInvoice.getCreateAt());

        assertThat(total).isEqualTo(1);
        assertThat(approved).isEqualTo(0);
        assertThat(rejected).isEqualTo(0);
        assertThat(unapproved).isEqualTo(1);
    }

}
