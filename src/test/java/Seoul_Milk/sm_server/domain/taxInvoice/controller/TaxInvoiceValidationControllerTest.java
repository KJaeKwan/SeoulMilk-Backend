package Seoul_Milk.sm_server.domain.taxInvoice.controller;

import static Seoul_Milk.sm_server.domain.taxInvoice.enums.CodefParameters.APPROVAL_NO;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.CodefParameters.CONTRACTOR_REG_NUMBER;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.CodefParameters.ID;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.CodefParameters.IDENTITY;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.CodefParameters.LOGIN_TYPE;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.CodefParameters.LOGIN_TYPE_LEVEL;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.CodefParameters.ORGANIZATION;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.CodefParameters.ORIGINAL_APPROVAL_NO;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.CodefParameters.PHONE_NO;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.CodefParameters.REPORTING_DATE;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.CodefParameters.SUPPLIER_REG_NUMBER;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.CodefParameters.SUPPLY_VALUE;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.CodefParameters.TELECOM;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.CodefParameters.USER_NAME;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus.APPROVED;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus.PENDING;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.TwoWayInfo.JOB_INDEX;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.TwoWayInfo.JTI;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.TwoWayInfo.THREAD_INDEX;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.TwoWayInfo.TWO_WAY_TIMESTAMP;
import static Seoul_Milk.sm_server.global.common.exception.ErrorCode.CODEF_NEED_AUTHENTICATION;
import static Seoul_Milk.sm_server.global.common.response.result.ResponseState.SUCCESS;
import static Seoul_Milk.sm_server.util.TaxDataCreatorUtil.createTaxInvoice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import Seoul_Milk.sm_server.domain.member.entity.MemberEntity;
import Seoul_Milk.sm_server.domain.taxInvoice.dto.validation.request.NonVerifiedTaxValidationRequestDTO;
import Seoul_Milk.sm_server.domain.taxInvoice.dto.validation.request.TaxInvoiceInfo;
import Seoul_Milk.sm_server.domain.taxInvoice.dto.validation.request.VerifiedTaxValidationRequestDTO;
import Seoul_Milk.sm_server.domain.taxInvoice.dto.validation.response.NonVerifiedTaxValidationResponseDTO;
import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import Seoul_Milk.sm_server.domain.taxInvoice.repository.TaxInvoiceRepository;
import Seoul_Milk.sm_server.global.common.exception.CustomException;
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
        mockRedisgetCodefApiResponse();
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

    /**
     * 진위여부 true인 경우
     * @throws JsonProcessingException
     * @throws UnsupportedEncodingException
     * @throws InterruptedException
     */
    private void mockCodefResultRealResponse()
            throws JsonProcessingException, UnsupportedEncodingException, InterruptedException {
        Map<String, Object> result = new HashMap<>();
        result.put("code", "CF-00000");
        result.put("message", "정상 처리되었습니다.");

        Map<String, Object> data = new HashMap<>();
        data.put("resAuthenticity", "1"); // 진위여부 true

        Map<String, Object> mockResponseMap = new HashMap<>();
        mockResponseMap.put("result", result);
        mockResponseMap.put("data", data);

        ObjectMapper objectMapper = new ObjectMapper();
        String mockResponse = objectMapper.writeValueAsString(mockResponseMap);

        when(easyCodef.requestCertification(
                anyString(),
                eq(EasyCodefServiceType.DEMO),
                any(HashMap.class)
        )).thenReturn(mockResponse);
    }

    /**
     * 진위여부 false인 경우
     * @throws JsonProcessingException
     * @throws UnsupportedEncodingException
     * @throws InterruptedException
     */
    private void mockCodefResultFakeResponse()
            throws JsonProcessingException, UnsupportedEncodingException, InterruptedException {
        Map<String, Object> result = new HashMap<>();
        result.put("code", "CF-00000");
        result.put("message", "정상 처리되었습니다.");

        Map<String, Object> data = new HashMap<>();
        data.put("resAuthenticity", "0"); // 진위여부 false

        Map<String, Object> mockResponseMap = new HashMap<>();
        mockResponseMap.put("result", result);
        mockResponseMap.put("data", data);

        ObjectMapper objectMapper = new ObjectMapper();
        String mockResponse = objectMapper.writeValueAsString(mockResponseMap);

        when(easyCodef.requestCertification(
                anyString(),
                eq(EasyCodefServiceType.DEMO),
                any(HashMap.class)
        )).thenReturn(mockResponse);
    }

    /**
     * 간편인증 안 하고 요청 시
     * @throws UnsupportedEncodingException
     * @throws JsonProcessingException
     * @throws InterruptedException
     */
    private void mockCodefResultNotAuthResponse()
            throws UnsupportedEncodingException, JsonProcessingException, InterruptedException {
        Map<String, Object> result = new HashMap<>();
        result.put("code", "CF-03002");
        result.put("message", "인증 필요");

        Map<String, Object> data = new HashMap<>();
        data.put("resAuthenticity", "0"); // 진위여부 false

        Map<String, Object> mockResponseMap = new HashMap<>();
        mockResponseMap.put("result", result);
        mockResponseMap.put("data", data);

        ObjectMapper objectMapper = new ObjectMapper();
        String mockResponse = objectMapper.writeValueAsString(mockResponseMap);

        when(easyCodef.requestCertification(
                anyString(),
                eq(EasyCodefServiceType.DEMO),
                any(HashMap.class)
        )).thenReturn(mockResponse);
    }

    private void mockRedisgetCodefApiResponse(){
        Map<String, Object> authResponse = new HashMap<>();
        authResponse.put(ORGANIZATION.getKey(), "0004"); // 기관 코드
        authResponse.put(ID.getKey(), MEMBER_UNIQUE_ID); // 사용자 ID
        authResponse.put(LOGIN_TYPE.getKey(), "5");      // 로그인 타입
        authResponse.put(JOB_INDEX.name(), "0");
        authResponse.put(THREAD_INDEX.name(), "0");
        authResponse.put(JTI.name(), "5858a9371e9c43e78f0f2739a941d529");
        authResponse.put(TWO_WAY_TIMESTAMP.name(), "1569910218149");

        Map<String, Object> commonResponse = new HashMap<>();
        commonResponse.put(LOGIN_TYPE_LEVEL.getKey(), "1");
        commonResponse.put(USER_NAME.getKey(), "김영록");
        commonResponse.put(PHONE_NO.getKey(), "01012345678");
        commonResponse.put(IDENTITY.getKey(), "010327");
        commonResponse.put(TELECOM.getKey(), "1");

        Map<String, Object> firstResponse = new HashMap<>();
        firstResponse.put(SUPPLIER_REG_NUMBER.getKey(), "1");
        firstResponse.put(CONTRACTOR_REG_NUMBER.getKey(), "1");
        firstResponse.put(APPROVAL_NO.getKey(), "1");
        firstResponse.put(REPORTING_DATE.getKey(), "2024-03-26");
        firstResponse.put(SUPPLY_VALUE.getKey(), "1");
        firstResponse.put(ORIGINAL_APPROVAL_NO.getKey(), "1");

        when(testContainer.redisUtils.getCodefApiResponse(MEMBER_UNIQUE_ID)).thenReturn(authResponse);
        when(testContainer.redisUtils.getCodefApiResponse(MEMBER_UNIQUE_ID + "common")).thenReturn(commonResponse);
        when(testContainer.redisUtils.getCodefApiResponse(MEMBER_UNIQUE_ID + "first")).thenReturn(firstResponse);
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
        assertThat(response.getResult().getKey()).isEqualTo(MEMBER_UNIQUE_ID);
    }

    @Test
    @DisplayName("진위여부 파악 테스트(세금계산서가 진짜 일떄)")
    void realTaxInvoiceController() throws UnsupportedEncodingException, JsonProcessingException, InterruptedException {
        mockCodefResultRealResponse();
        TaxInvoice taxInvoice = createTaxInvoice(1L, "1", PENDING, "1", "1", "2024-03-26", null, "서울우유 1", testMember);
        testContainer.taxInvoiceRepository.save(taxInvoice);
        VerifiedTaxValidationRequestDTO verifiedTaxValidationRequestDTO
                = VerifiedTaxValidationRequestDTO
                .builder()
                .key(MEMBER_UNIQUE_ID)
                .build();

        SuccessResponse<String> response =
                testContainer.taxInvoiceValidationController.verifiedTaxvalidation(verifiedTaxValidationRequestDTO);
        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo(SUCCESS.getCode());
        assertThat(response.getResult()).isEqualTo("성공");
    }

    @Test
    @DisplayName("진위여부 파악 테스트(세금계산서가 가짜 일떄)")
    void fakeTaxInvoiceController() throws UnsupportedEncodingException, JsonProcessingException, InterruptedException {
        mockCodefResultFakeResponse();
        TaxInvoice taxInvoice = createTaxInvoice(1L, "1", PENDING, "1", "1", "2024-03-26", null, "서울우유 1", testMember);
        testContainer.taxInvoiceRepository.save(taxInvoice);
        VerifiedTaxValidationRequestDTO verifiedTaxValidationRequestDTO
                = VerifiedTaxValidationRequestDTO
                .builder()
                .key(MEMBER_UNIQUE_ID)
                .build();

        SuccessResponse<String> response =
                testContainer.taxInvoiceValidationController.verifiedTaxvalidation(verifiedTaxValidationRequestDTO);
        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo(SUCCESS.getCode());
        assertThat(response.getResult()).isEqualTo("성공");
    }

    @Test
    @DisplayName("진위여부 파악 테스트(간편인증을 안 했을 때)")
    void notAuthController() throws UnsupportedEncodingException, JsonProcessingException, InterruptedException {
        mockCodefResultNotAuthResponse();
        TaxInvoice taxInvoice = createTaxInvoice(1L, "1", PENDING, "1", "1", "2024-03-26", null, "서울우유 1", testMember);
        testContainer.taxInvoiceRepository.save(taxInvoice);
        VerifiedTaxValidationRequestDTO verifiedTaxValidationRequestDTO
                = VerifiedTaxValidationRequestDTO
                .builder()
                .key(MEMBER_UNIQUE_ID)
                .build();
        assertThatThrownBy(() -> testContainer.taxInvoiceValidationController.verifiedTaxvalidation(verifiedTaxValidationRequestDTO))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CODEF_NEED_AUTHENTICATION.getMessage());

    }
}
