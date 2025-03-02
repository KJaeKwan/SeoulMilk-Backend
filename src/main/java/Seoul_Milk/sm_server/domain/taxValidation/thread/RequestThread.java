package Seoul_Milk.sm_server.domain.taxValidation.thread;

import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import Seoul_Milk.sm_server.domain.taxInvoice.repository.TaxInvoiceRepository;
import Seoul_Milk.sm_server.global.redis.RedisUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.codef.api.EasyCodef;
import io.codef.api.EasyCodefServiceType;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static Seoul_Milk.sm_server.domain.taxValidation.enums.CodefParameters.*;
import static Seoul_Milk.sm_server.domain.taxValidation.enums.CodefResponseCode.NEED_SIMPLE_AUTHENTICATION;
import static Seoul_Milk.sm_server.domain.taxValidation.enums.TwoWayInfo.*;

public class RequestThread extends Thread {
    private final EasyCodef codef;
    private final HashMap<String, Object> parameterMap;
    private final int threadNo;
    private final String productUrl;
    private final String id;

    private final RedisUtils redisUtils;
    private final TaxInvoiceRepository taxInvoiceRepository;

    public RequestThread(String id, EasyCodef codef, HashMap<String, Object> parameterMap, int threadNo, String productUrl,
            RedisUtils redisUtils, TaxInvoiceRepository taxInvoiceRepository) {
        this.codef = codef;
        this.parameterMap = parameterMap;
        this.threadNo = threadNo;
        this.productUrl = productUrl;
        this.id = id;
        this.redisUtils = redisUtils;
        this.taxInvoiceRepository = taxInvoiceRepository;
    }

    @Override
    public void run() {

        String result;
        String code;
        boolean continue2Way = false;

        try {
            result = codef.requestProduct(productUrl, EasyCodefServiceType.DEMO, parameterMap);
        } catch (UnsupportedEncodingException | JsonProcessingException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        HashMap<String, Object> responseMap;
        try {
            responseMap = new ObjectMapper().readValue(result, HashMap.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        HashMap<String, Object> resultMap = (HashMap<String, Object>)responseMap.get("result");

        //추가 인증이 필요한 경우 result 객체의 응답코드가 CF-03002
        code = (String)resultMap.get("code");
        System.out.println("응답코드 : " + code);

        HashMap<String, Object> dataMap = (HashMap<String, Object>)responseMap.get("data");

        // data객체에 continue2Way 필드가 존재하는지 확인
        if (dataMap.containsKey("continue2Way")) {
            continue2Way = (boolean) dataMap.get("continue2Way");
        }

        // 응답코드가 CF-03002 이고 continue2Way 필드가 true인 경우 추가 인증 정보를 변수에 저장
        if (NEED_SIMPLE_AUTHENTICATION.isEqual(code) && continue2Way){
            redisUtils.saveCodefApiResponse(id, Map.of(
                    JOB_INDEX.name(), dataMap.get(JOB_INDEX.setCarmelCase()),
                    THREAD_INDEX.name(), dataMap.get(THREAD_INDEX.setCarmelCase()),
                    JTI.name(), dataMap.get(JTI.setCarmelCase()),
                    TWO_WAY_TIMESTAMP.name(), dataMap.get(TWO_WAY_TIMESTAMP.setCarmelCase())
            ));
            redisUtils.saveCodefApiResponse(id+"common", Map.of(
                    LOGIN_TYPE_LEVEL.getKey(),parameterMap.get(LOGIN_TYPE_LEVEL.getKey()),
                    USER_NAME.getKey(), parameterMap.get(USER_NAME.getKey()),
                    PHONE_NO.getKey(), parameterMap.get(PHONE_NO.getKey()),
                    IDENTITY.getKey(), parameterMap.get(IDENTITY.getKey()),
                    TELECOM.getKey(), parameterMap.get(TELECOM.getKey())
            ));
            redisUtils.saveCodefApiResponse(id+"first", Map.of(
                    SUPPLIER_REG_NUMBER.getKey(), parameterMap.get(SUPPLIER_REG_NUMBER.getKey()),
                    CONTRACTOR_REG_NUMBER.getKey(), parameterMap.get(CONTRACTOR_REG_NUMBER.getKey()),
                    APPROVAL_NO.getKey(), parameterMap.get(APPROVAL_NO.getKey()),
                    REPORTING_DATE.getKey(), parameterMap.get(REPORTING_DATE.getKey()),
                    SUPPLY_VALUE.getKey(), parameterMap.get(SUPPLY_VALUE.getKey())
            ));
        }
        if(threadNo > 0){
            TaxInvoice taxInvoice = taxInvoiceRepository.findByIssueId(parameterMap.get(APPROVAL_NO.getKey()).toString());
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = null;
            try {
                rootNode = objectMapper.readTree(result);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            String resAuthenticity = rootNode.path("data").path("resAuthenticity").asText();
            if(Objects.equals(resAuthenticity, "1")){
                taxInvoice.approve();
            }else{
                taxInvoice.reject();
            }
            taxInvoiceRepository.save(taxInvoice);
        }
    }
}
