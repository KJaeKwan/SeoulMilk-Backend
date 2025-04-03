package Seoul_Milk.sm_server.domain.taxInvoice.controller;

import static Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus.APPROVED;
import static Seoul_Milk.sm_server.global.common.response.result.ResponseState.SUCCESS;
import static Seoul_Milk.sm_server.util.TaxDataCreatorUtil.createTaxInvoice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import Seoul_Milk.sm_server.domain.member.entity.MemberEntity;
import Seoul_Milk.sm_server.domain.taxInvoice.dto.validation.request.NonVerifiedTaxValidationRequestDTO;
import Seoul_Milk.sm_server.domain.taxInvoice.dto.validation.request.TaxInvoiceInfo;
import Seoul_Milk.sm_server.domain.taxInvoice.dto.validation.response.NonVerifiedTaxValidationResponseDTO;
import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import Seoul_Milk.sm_server.domain.taxInvoice.repository.TaxInvoiceRepository;
import Seoul_Milk.sm_server.global.common.response.SuccessResponse;
import Seoul_Milk.sm_server.global.infrastructure.redis.RedisUtils;
import Seoul_Milk.sm_server.mock.container.TestContainer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.codef.api.EasyCodef;
import io.codef.api.EasyCodefServiceType;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TaxInvoiceValidationControllerTest {

    private static final String MEMBER_UNIQUE_ID = "111";
    private TestContainer testContainer;
    @Mock
    private MemberEntity testMember;
    @Mock
    private EasyCodef easyCodef;
    @Mock
    private Thread thread;
    @BeforeEach
    void setUp() throws UnsupportedEncodingException, JsonProcessingException, InterruptedException {
        MockitoAnnotations.openMocks(this);
        testContainer = new TestContainer();
        mockMemberSetting();
        mockCreateEasyCodef();
        mockCreateRequestThread();
        mockMemberUUID();
        mockCodefRequestProduct();
        threadMocking();
    }
    private void mockMemberSetting(){
        when(testMember.makeUniqueId()).thenReturn(MEMBER_UNIQUE_ID);
        when(testMember.getName()).thenReturn("김영록");
        when(testMember.getEmail()).thenReturn("praoo800@naver.com");
        when(testMember.getEmployeeId()).thenReturn("202011269");
        when(testMember.getId()).thenReturn(1L);
    }

    private void mockCreateEasyCodef(){
        when(testContainer.codefFactory.create())
                .thenReturn(easyCodef);
    }

    private void mockCreateRequestThread(){
        when(testContainer.requestThreadFactory.create(
                anyString(), any(EasyCodef.class), any(HashMap.class), anyInt(),
                anyString(), any(RedisUtils.class), any(TaxInvoiceRepository.class),
                anyString()
        )).thenReturn(thread);
    }

    private void mockMemberUUID(){
        when(testMember.makeUniqueId())
                .thenReturn(MEMBER_UNIQUE_ID);
    }

    private void mockCodefRequestProduct()
            throws UnsupportedEncodingException, JsonProcessingException, InterruptedException {
        Map<String, Object> result = new HashMap<>();
        result.put("code", "CF-03002"); // 추가 인증 필요한 상태
        result.put("message", "추가 인증이 필요합니다.");

        Map<String, Object> data = new HashMap<>();
        data.put("continue2Way", true);
        data.put("method", "추가인증방식");
        data.put("jobIndex", 0);
        data.put("threadIndex", 0);
        data.put("jti", "5858a9371e9c43e78f0f2739a941d529");
        data.put("twoWayTimestamp", "1569910218149");

        Map<String, Object> codefResponse = new HashMap<>();
        codefResponse.put("result", result);
        codefResponse.put("data", data);

        String mockResponse = new ObjectMapper().writeValueAsString(codefResponse);

        when(easyCodef.requestProduct(anyString(), any(EasyCodefServiceType.class), any(HashMap.class)))
                .thenReturn(mockResponse);
    }

    private void threadMocking(){
        doNothing().when(thread).start();
    }

    @Test
    @DisplayName("초기 인증 컨트롤러 테스트")
    void initialCodefRequestController() throws ExecutionException, InterruptedException {
        TaxInvoice taxInvoice = createTaxInvoice(1L, "1", APPROVED, "1", "1", "2024-03-26", null, "서울우유 1", testMember);
        testContainer.taxInvoiceRepository.save(taxInvoice);
        TaxInvoiceInfo taxInvoiceInfo = TaxInvoiceInfo.builder()
                .approvalNo(taxInvoice.getIssueId())
                .contractorRegNumber(taxInvoice.getIpId())
                .supplierRegNumber(taxInvoice.getSuId())
                .reportingDate(taxInvoice.getErDat())
                .supplyValue(String.valueOf(taxInvoice.getChargeTotal()))
                .build();

        NonVerifiedTaxValidationRequestDTO nonVerifiedTaxValidationRequestDTO
                = NonVerifiedTaxValidationRequestDTO.builder()
                .loginTypeLevel("1")
                .userName("김영록")
                .phoneNo("01012345678")
                .identity("20010327")
                .telecom("1")
                .taxInvoiceInfoList(List.of(taxInvoiceInfo))
                .build();
        SuccessResponse<NonVerifiedTaxValidationResponseDTO> response =
                testContainer.taxInvoiceValidationController.nonVerifiedTaxValidation(nonVerifiedTaxValidationRequestDTO, testMember);

        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo(SUCCESS.getCode());
    }
}
