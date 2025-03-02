package Seoul_Milk.sm_server.domain.taxValidation.thread;

import Seoul_Milk.sm_server.domain.taxInvoice.constant.ProcessStatus;
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
        if (code.equals("CF-03002") && continue2Way){
            redisUtils.saveCodefApiResponse(id, Map.of(
                    "JOB_INDEX", dataMap.get("jobIndex"),
                    "THREAD_INDEX", dataMap.get("threadIndex"),
                    "JTI", dataMap.get("jti"),
                    "TWO_WAY_TIMESTAMP", dataMap.get("twoWayTimestamp")
            ));
            redisUtils.saveCodefApiResponse(id+"common", Map.of(
                    "loginTypeLevel",parameterMap.get("loginTypeLevel"),
                    "userName",parameterMap.get("userName"),
                    "phoneNo",parameterMap.get("phoneNo"),
                    "identity",parameterMap.get("identity"),
                    "telecom",parameterMap.get("telecom")
            ));
            redisUtils.saveCodefApiResponse(id+"first", Map.of(
                "supplierRegNumber", parameterMap.get("supplierRegNumber"),
                "contractorRegNumber", parameterMap.get("contractorRegNumber"),
                "approvalNo",parameterMap.get("approvalNo"),
                "reportingDate",parameterMap.get("reportingDate"),
                "supplyValue",parameterMap.get("supplyValue")
            ));
        }
        if(threadNo > 0){
            TaxInvoice taxInvoice = taxInvoiceRepository.findByIssueId(parameterMap.get("approvalNo").toString());
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = null;
            try {
                rootNode = objectMapper.readTree(result);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            String resAuthenticity = rootNode.path("data").path("resAuthenticity").asText();
            if(Objects.equals(resAuthenticity, "1")){
                taxInvoice.changeStatus(ProcessStatus.APPROVED);
            }else{
                taxInvoice.changeStatus(ProcessStatus.REJECTED);
            }
            taxInvoiceRepository.save(taxInvoice);
        }
    }
}
