package Seoul_Milk.sm_server.domain.taxValidation.service;

import Seoul_Milk.sm_server.domain.taxInvoice.constant.ProcessStatus;
import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import Seoul_Milk.sm_server.domain.taxInvoice.repository.TaxInvoiceJpaRepository;
import Seoul_Milk.sm_server.domain.taxInvoice.repository.TaxInvoiceRepository;
import Seoul_Milk.sm_server.domain.taxInvoice.repository.TaxInvoiceRepositoryImpl;
import Seoul_Milk.sm_server.domain.taxValidation.dto.NonVerifiedTaxValidationRequestDTO;
import Seoul_Milk.sm_server.domain.taxValidation.dto.NonVerifiedTaxValidationResponseDTO;
import Seoul_Milk.sm_server.domain.taxValidation.dto.TaxInvoiceInfo;
import Seoul_Milk.sm_server.domain.taxValidation.dto.TestDTO;
import Seoul_Milk.sm_server.domain.taxValidation.dto.TestTaxInvoice;
import Seoul_Milk.sm_server.domain.taxValidation.thread.RequestThread;
import Seoul_Milk.sm_server.global.redis.RedisUtils;
import Seoul_Milk.sm_server.login.entity.MemberEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.codef.api.EasyCodef;
import io.codef.api.EasyCodefServiceType;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TaxValidationServiceImpl implements TaxValidationService {
    private final String PUBLIC_KEY;
    private final String CLIENT_ID;
    private final String CLIENT_SECRET;
    private final String PRODUCT_URL;
    private final RedisUtils redisUtils;
    private final TaxInvoiceRepository taxInvoiceRepository;
    public TaxValidationServiceImpl(
            @Value("${codef.public.key}") String PUBLIC_KEY,
            @Value("${codef.client.id}") String CLIENT_ID,
            @Value("${codef.client.secret}") String CLIENT_SECRET,
            @Value("${codef.valid.url}") String productUrl, RedisUtils redisUtils,
            TaxInvoiceRepository taxInvoiceRepository
    ) {
        this.PUBLIC_KEY = PUBLIC_KEY;
        this.CLIENT_ID = CLIENT_ID;
        this.CLIENT_SECRET = CLIENT_SECRET;
        this.PRODUCT_URL = productUrl;
        this.redisUtils = redisUtils;
        this.taxInvoiceRepository = taxInvoiceRepository;
    }
    private EasyCodef settingCodef(){
        EasyCodef easyCodef = new EasyCodef();
        easyCodef.setClientInfoForDemo(CLIENT_ID, CLIENT_SECRET);
        easyCodef.setPublicKey(PUBLIC_KEY);
        return easyCodef;
    }
    @Override
    public NonVerifiedTaxValidationResponseDTO nonVerifiedTaxValidation(
            NonVerifiedTaxValidationRequestDTO nonVerifiedTaxValidationRequestDTO,
            MemberEntity memberEntity)
            throws InterruptedException {
        EasyCodef easyCodef = settingCodef();
        List<TaxInvoiceInfo> taxInvoiceInfoList = nonVerifiedTaxValidationRequestDTO.getTaxInvoiceInfoList();
        String id = memberEntity.makeUniqueId();
        int iter = taxInvoiceInfoList.size();
        for(int i=0; i<iter; i++) {
            TaxInvoiceInfo taxInvoiceInfo = taxInvoiceInfoList.get(i);
            HashMap<String, Object> requestData = new HashMap<String, Object>();
            requestData.put("organization", "0004");
            requestData.put("loginType", "5");
            requestData.put("id", id); // TODO 나중에 사번 + uuid형식으로 해야함
            requestData.put("loginTypeLevel", nonVerifiedTaxValidationRequestDTO.getLoginTypeLevel());
            requestData.put("userName", nonVerifiedTaxValidationRequestDTO.getUserName());
            requestData.put("phoneNo", nonVerifiedTaxValidationRequestDTO.getPhoneNo());
            requestData.put("identity", nonVerifiedTaxValidationRequestDTO.getIdentity());
            requestData.put("telecom", nonVerifiedTaxValidationRequestDTO.getTelecom());
            requestData.put("supplierRegNumber", taxInvoiceInfo.getSupplierRegNumber());
            requestData.put("contractorRegNumber", taxInvoiceInfo.getContractorRegNumber());
            requestData.put("approvalNo", taxInvoiceInfo.getApprovalNo());
            requestData.put("reportingDate", taxInvoiceInfo.getReportingDate());
            requestData.put("supplyValue", taxInvoiceInfo.getSupplyValue());

            Thread t = new RequestThread(id, easyCodef, requestData, i, PRODUCT_URL, redisUtils, taxInvoiceRepository);

            t.start();

            // API 요청A와 요청B 다건 요청을 위해서는 요청A 처리 후 요청B를 처리할 수 있도록
            // 요청A 송신 후 약 0.5초 ~ 1초 이내 요청B 송신 필요
            Thread.sleep(1000);
        }
        return new NonVerifiedTaxValidationResponseDTO(id);
    }

    @Override
    public String verifiedTaxValidation(String key)
            throws UnsupportedEncodingException, JsonProcessingException, InterruptedException {
        EasyCodef easyCodef = settingCodef();
        HashMap<String, Object> parameterMap = new HashMap<String, Object>();
        Map<String, Object> addAuthResponse = redisUtils.getCodefApiResponse(key);
        Map<String, Object> commonResponse = redisUtils.getCodefApiResponse(key+"common");
        Map<String, Object> firstResponse = redisUtils.getCodefApiResponse(key+"first");
        parameterMap.put("organization", "0004"); //기관코드 필수 입력
        parameterMap.put("id", key); //식별아이디 필수 입력
        parameterMap.put("loginType", "5");
        parameterMap.put("loginTypeLevel",commonResponse.get("loginTypeLevel"));
        parameterMap.put("userName", commonResponse.get("userName"));
        parameterMap.put("phoneNo", commonResponse.get("phoneNo"));
        parameterMap.put("identity", commonResponse.get("identity"));
        parameterMap.put("supplierRegNumber", firstResponse.get("supplierRegNumber"));
        parameterMap.put("contractorRegNumber", firstResponse.get("contractorRegNumber"));
        parameterMap.put("approvalNo", firstResponse.get("approvalNo"));
        parameterMap.put("reportingDate", firstResponse.get("reportingDate"));
        parameterMap.put("supplyValue", firstResponse.get("supplyValue"));
        parameterMap.put("telecom", commonResponse.get("telecom"));

//간편인증 추가인증 입력부
        parameterMap.put("simpleAuth", "1");
        parameterMap.put("is2Way", true);

/** #3.twoWayInfo 파라미터 설정*/
        HashMap<String, Object> twoWayInfo = new HashMap<String, Object>();

        System.out.println(addAuthResponse);
        twoWayInfo.put("jobIndex", Integer.parseInt((String) addAuthResponse.get("JOB_INDEX")));
        twoWayInfo.put("threadIndex", Integer.parseInt((String) addAuthResponse.get("THREAD_INDEX")));
        twoWayInfo.put("jti",  addAuthResponse.get("JTI"));
        twoWayInfo.put("twoWayTimestamp", Long.parseLong((String) addAuthResponse.get("TWO_WAY_TIMESTAMP")));

        parameterMap.put("twoWayInfo", twoWayInfo);

// 요청 Endpoint는 동일함
        String result;

// 추가인증 요청 시에는 이지코드에프.requestCertification 으로 호출
        result = easyCodef.requestCertification(PRODUCT_URL, EasyCodefServiceType.DEMO, parameterMap);
        System.out.println(firstResponse.get("approvalNo").toString());
        TaxInvoice taxInvoice = taxInvoiceRepository.findByIssueId(firstResponse.get("approvalNo").toString());
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

/** #4.결과값 확인 */
        System.out.println("요청A(추가인증) result : " + result);
        return "성공";
    }

    @Override
    public TestDTO insertDB() {
        ArrayList<TestTaxInvoice> taxInvoiceArrayList = new ArrayList<>();
        for(int i=0; i<47; i++){
            Random random = new Random();
            StringBuilder sb = new StringBuilder();
            TestTaxInvoice testTaxInvoice = new TestTaxInvoice();
            for (int j = 0; j < 16; j++) {
                sb.append(random.nextInt(10)); // 0~9 사이의 숫자 추가
            }
            TaxInvoice taxInvoice = TaxInvoice.create(
                    "20240630"+ sb.toString(),
                    "3058148738",
                    "3050777873",
                    250909,
                    "20240630",
                    "k",
                    null,
                    null,
                    null
            );
            testTaxInvoice.setApprovalNo("20240630" + sb.toString());
            testTaxInvoice.setReportingDate("20240630");
            testTaxInvoice.setSupplierRegNumber("3050777873");
            testTaxInvoice.setContractorRegNumber("3058148738");
            testTaxInvoice.setSupplyValue("250909");
            taxInvoiceRepository.save(taxInvoice);
            taxInvoiceArrayList.add(testTaxInvoice);
        }
        return new TestDTO(taxInvoiceArrayList);
    }

}
