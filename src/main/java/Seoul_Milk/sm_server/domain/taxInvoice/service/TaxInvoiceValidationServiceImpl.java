package Seoul_Milk.sm_server.domain.taxInvoice.service;

import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import Seoul_Milk.sm_server.domain.taxInvoice.repository.TaxInvoiceRepository;
import Seoul_Milk.sm_server.domain.taxInvoice.dto.validation.request.NonVerifiedTaxValidationRequestDTO;
import Seoul_Milk.sm_server.domain.taxInvoice.dto.validation.request.TaxInvoiceInfo;
import Seoul_Milk.sm_server.domain.taxInvoice.dto.validation.response.NonVerifiedTaxValidationResponseDTO;
import Seoul_Milk.sm_server.domain.taxInvoice.thread.RequestThreadFactory;
import Seoul_Milk.sm_server.domain.taxInvoice.thread.RequestThreadFactoryImpl;
import Seoul_Milk.sm_server.domain.taxInvoice.thread.RequestThreadManager;
import Seoul_Milk.sm_server.global.common.exception.CustomException;
import Seoul_Milk.sm_server.global.infrastructure.codef.CodefFactory;
import Seoul_Milk.sm_server.global.infrastructure.codef.CodefFactoryImpl;
import Seoul_Milk.sm_server.global.infrastructure.redis.RedisUtils;
import Seoul_Milk.sm_server.domain.member.entity.MemberEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.codef.api.EasyCodef;
import io.codef.api.EasyCodefServiceType;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static Seoul_Milk.sm_server.domain.taxInvoice.enums.CodefParameters.*;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.CodefResponseCode.*;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.ThreadTerm.THREAD_TERM;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.TwoWayInfo.*;
import static Seoul_Milk.sm_server.global.common.exception.ErrorCode.*;

@Service
@Builder
public class TaxInvoiceValidationServiceImpl implements TaxInvoiceValidationService {
    private final String PRODUCT_URL;
    private final RedisUtils redisUtils;
    private final TaxInvoiceRepository taxInvoiceRepository;
    private final CodefFactory codefFactory;
    private final RequestThreadFactory requestThreadFactory;
    public TaxInvoiceValidationServiceImpl(
            @Value("${codef.valid.url}") String productUrl, RedisUtils redisUtils,
            TaxInvoiceRepository taxInvoiceRepository, CodefFactory codefFactory,
            RequestThreadFactory requestThreadFactory
    ) {
        this.PRODUCT_URL = productUrl;
        this.redisUtils = redisUtils;
        this.taxInvoiceRepository = taxInvoiceRepository;
        this.codefFactory = codefFactory;
        this.requestThreadFactory = requestThreadFactory;
    }
    @Override
    public NonVerifiedTaxValidationResponseDTO nonVerifiedTaxValidation(
            NonVerifiedTaxValidationRequestDTO nonVerifiedTaxValidationRequestDTO,
            MemberEntity memberEntity)
            throws InterruptedException {
        EasyCodef easyCodef = codefFactory.create();
        List<TaxInvoiceInfo> taxInvoiceInfoList = nonVerifiedTaxValidationRequestDTO.getTaxInvoiceInfoList();
        String id = memberEntity.makeUniqueId();
        int iter = taxInvoiceInfoList.size();

        //codef api의 요청 전 요청 값 검사(진위여부 확인하려는 세금계산서가 본인 것이 맞는지)
        if (taxInvoiceInfoList.stream()
                .anyMatch(taxInvoiceInfo ->
                        !taxInvoiceRepository.isAccessYourTaxInvoice(memberEntity, taxInvoiceInfo.getApprovalNo()))) {
            throw new CustomException(DO_NOT_ACCESS_OTHER_TAX_INVOICE);
        }


        for(int i=0; i<iter; i++) {
            TaxInvoiceInfo taxInvoiceInfo = taxInvoiceInfoList.get(i);

            // 공통 파라미터 설정
            HashMap<String, Object> requestData = populateParameters(id, Map.of(
                    LOGIN_TYPE_LEVEL.getKey(), nonVerifiedTaxValidationRequestDTO.getLoginTypeLevel(),
                    USER_NAME.getKey(), nonVerifiedTaxValidationRequestDTO.getUserName(),
                    PHONE_NO.getKey(), nonVerifiedTaxValidationRequestDTO.getPhoneNo(),
                    IDENTITY.getKey(), nonVerifiedTaxValidationRequestDTO.getIdentity(),
                    TELECOM.getKey(), nonVerifiedTaxValidationRequestDTO.getTelecom()
            ), Map.of(
                    SUPPLIER_REG_NUMBER.getKey(), taxInvoiceInfo.getSupplierRegNumber().replaceAll("-", ""),
                    CONTRACTOR_REG_NUMBER.getKey(), taxInvoiceInfo.getContractorRegNumber().replaceAll("-", ""),
                    APPROVAL_NO.getKey(), taxInvoiceInfo.getApprovalNo().replaceAll("-", ""),
                    REPORTING_DATE.getKey(), taxInvoiceInfo.getReportingDate().replaceAll("-", ""),
                    SUPPLY_VALUE.getKey(), taxInvoiceInfo.getSupplyValue().replaceAll(",", "")
            ));

            Thread t = requestThreadFactory.create(id, easyCodef, requestData, i, PRODUCT_URL, redisUtils, taxInvoiceRepository, taxInvoiceInfo.getApprovalNo());

            t.start();

            // API 요청A와 요청B 다건 요청을 위해서는 요청A 처리 후 요청B를 처리할 수 있도록
            // 요청A 송신 후 약 0.5초 ~ 1초 이내 요청B 송신 필요
            Thread.sleep(THREAD_TERM.getMillis());
        }
        return new NonVerifiedTaxValidationResponseDTO(id);
    }

    @Override
    public String verifiedTaxValidation(String key)
            throws UnsupportedEncodingException, JsonProcessingException, InterruptedException {
        EasyCodef easyCodef = codefFactory.create();

        Map<String, Object> addAuthResponse = redisUtils.getCodefApiResponse(key);
        Map<String, Object> commonResponse = redisUtils.getCodefApiResponse(key+"common");
        Map<String, Object> firstResponse = redisUtils.getCodefApiResponse(key+"first");
        String originalApproveNo = firstResponse.get(ORIGINAL_APPROVAL_NO.getKey()).toString();
        firstResponse.remove(ORIGINAL_APPROVAL_NO.getKey());

        HashMap<String, Object> parameterMap = populateParameters(key, commonResponse, firstResponse);
        parameterMap.remove(ORIGINAL_APPROVAL_NO.getKey());
        //간편인증 추가인증 입력부
        parameterMap.put("simpleAuth", "1");
        parameterMap.put("is2Way", true);

        /** #3.twoWayInfo 파라미터 설정*/
        HashMap<String, Object> twoWayInfo = twoWayInfoParameters(addAuthResponse);

        parameterMap.put(TWO_WAY_INFO.getKey(), twoWayInfo);

        // 요청 Endpoint는 동일함
        String result;

        // 추가인증 요청 시에는 이지코드에프.requestCertification 으로 호출
        result = easyCodef.requestCertification(PRODUCT_URL, EasyCodefServiceType.DEMO, parameterMap);

        TaxInvoice taxInvoice = taxInvoiceRepository.findByIssueId(originalApproveNo)
                .orElseThrow(() -> new CustomException(TAX_INVOICE_NOT_EXIST));
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode rootNode = null;
        try {
            rootNode = objectMapper.readTree(result);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String resAuthenticity = rootNode.path("data").path("resAuthenticity").asText();
        String code = rootNode.path("result").path("code").asText();
        validateCodefApi(code);
        if(Objects.equals(resAuthenticity, "1")){
            taxInvoice.approve();
        }else{
            taxInvoice.reject();
        }
        taxInvoiceRepository.save(taxInvoice);
        RequestThreadManager.waitForThreads(key);
        return "성공";
    }

    /**
     * codef api에서 올바른 응답이 오는지 확인하는 로직
     */
    private void validateCodefApi(String code){
        System.out.println(code);
        if(!SUCCESS_RESPONSE.isEqual(code)){
            if(NEED_SIMPLE_AUTHENTICATION.isEqual(code)){
                throw new CustomException(CODEF_NEED_AUTHENTICATION);
            }
            else if(!INVALID_APPROVE_NUM.isEqual(code) && !ERROR_APPROVE_NUM.isEqual(code)){ // 승인번호관련 에러(승인번호가 잘못되었을때)는 exception이 아니라 반려(reject)처리 해야하니깐
                throw new CustomException(CODEF_INTERANL_SERVER_ERROR);
            }
        }
    }

    /**
     * verifiedTaxValidation과
     * nonVerifiedTaxValidation이 공통으로 필요한 파라미터 넣는 메서드
     */
    private HashMap<String, Object> populateParameters(String id, Map<String, Object> commonResponse, Map<String, Object> firstResponse){
        HashMap<String, Object> parameterMap = new HashMap<>();

        // 필수 입력값 설정
        parameterMap.put(ORGANIZATION.getKey(), "0004");
        parameterMap.put(ID.getKey(), id);
        parameterMap.put(LOGIN_TYPE.getKey(), "5");

        // 공통 응답에서 가져오기

        parameterMap.put(LOGIN_TYPE_LEVEL.getKey(), commonResponse.get(LOGIN_TYPE_LEVEL.getKey()));
        parameterMap.put(USER_NAME.getKey(), commonResponse.get(USER_NAME.getKey()));
        parameterMap.put(PHONE_NO.getKey(), commonResponse.get(PHONE_NO.getKey()));
        parameterMap.put(IDENTITY.getKey(), commonResponse.get(IDENTITY.getKey()));
        parameterMap.put(TELECOM.getKey(), commonResponse.get(TELECOM.getKey()));


        // 첫 번째 응답에서 가져오기
        parameterMap.put(SUPPLIER_REG_NUMBER.getKey(), firstResponse.get(SUPPLIER_REG_NUMBER.getKey()));
        parameterMap.put(CONTRACTOR_REG_NUMBER.getKey(), firstResponse.get(CONTRACTOR_REG_NUMBER.getKey()));
        parameterMap.put(APPROVAL_NO.getKey(), firstResponse.get(APPROVAL_NO.getKey()));
        parameterMap.put(REPORTING_DATE.getKey(), firstResponse.get(REPORTING_DATE.getKey()));
        parameterMap.put(SUPPLY_VALUE.getKey(), firstResponse.get(SUPPLY_VALUE.getKey()));

        return parameterMap;
    }

    private HashMap<String, Object> twoWayInfoParameters(Map<String, Object> addAuthResponse){
        HashMap<String, Object> twoWayInfo = new HashMap<String, Object>();

        twoWayInfo.put(JOB_INDEX.setCarmelCase(), Integer.parseInt((String) addAuthResponse.get(JOB_INDEX.name())));
        twoWayInfo.put(THREAD_INDEX.setCarmelCase(), Integer.parseInt((String) addAuthResponse.get(THREAD_INDEX.name())));
        twoWayInfo.put(JTI.setCarmelCase(),  addAuthResponse.get(JTI.name()));
        twoWayInfo.put(TWO_WAY_TIMESTAMP.setCarmelCase(), Long.parseLong((String) addAuthResponse.get(TWO_WAY_TIMESTAMP.name())));
        return twoWayInfo;
    }
}
