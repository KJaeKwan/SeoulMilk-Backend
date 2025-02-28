package Seoul_Milk.sm_server.domain.taxValidation.service;

import Seoul_Milk.sm_server.domain.taxValidation.dto.NonVerifiedTaxValidationRequestDTO;
import Seoul_Milk.sm_server.domain.taxValidation.dto.TaxInvoiceInfo;
import Seoul_Milk.sm_server.domain.taxValidation.thread.RequestThread;
import Seoul_Milk.sm_server.login.entity.MemberEntity;
import io.codef.api.EasyCodef;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TaxValidationServiceImpl implements TaxValidationService {
    private final String PUBLIC_KEY;
    private final String CLIENT_ID;
    private final String CLIENT_SECRET;
    private final String PRODUCT_URL;
    public TaxValidationServiceImpl(
            @Value("${codef.public.key}") String PUBLIC_KEY,
            @Value("${codef.client.id}") String CLIENT_ID,
            @Value("${codef.client.secret}") String CLIENT_SECRET,
            @Value("${codef.valid.url}") String productUrl
            ) {
        this.PUBLIC_KEY = PUBLIC_KEY;
        this.CLIENT_ID = CLIENT_ID;
        this.CLIENT_SECRET = CLIENT_SECRET;
        this.PRODUCT_URL = productUrl;
    }
    private EasyCodef settingCodef(){
        EasyCodef easyCodef = new EasyCodef();
        easyCodef.setClientInfoForDemo(CLIENT_ID, CLIENT_SECRET);
        easyCodef.setPublicKey(PUBLIC_KEY);
        return easyCodef;
    }
    @Override
    public String nonVerifiedTaxValidation(
            NonVerifiedTaxValidationRequestDTO nonVerifiedTaxValidationRequestDTO,
            MemberEntity memberEntity)
            throws ExecutionException, InterruptedException {
        EasyCodef easyCodef = settingCodef();
        List<TaxInvoiceInfo> taxInvoiceInfoList = nonVerifiedTaxValidationRequestDTO.getTaxInvoiceInfoList();
        String id = "1234";
        int iter = taxInvoiceInfoList.size();
        for(int i=0; i<iter; i++) {
            TaxInvoiceInfo taxInvoiceInfo = taxInvoiceInfoList.get(i);
            HashMap<String, Object> requestData = new HashMap<String, Object>();
            requestData.put("organization", "0004");
            requestData.put("loginType", "5");
            requestData.put("id", memberEntity.makeUniqueId()); // TODO 나중에 사번 + uuid형식으로 해야함
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

            Thread t = new RequestThread(easyCodef, requestData, i, PRODUCT_URL);

            t.start();

            // API 요청A와 요청B 다건 요청을 위해서는 요청A 처리 후 요청B를 처리할 수 있도록
            // 요청A 송신 후 약 0.5초 ~ 1초 이내 요청B 송신 필요
            Thread.sleep(1000);
        }
        return "요청 완료 간편인증후에 api요청해주세요";
    }

//    private String processTaxInvoiceRequests(List<TaxInvoiceInfo> taxInvoiceInfoList,
//            NonVerifiedTaxValidationRequestDTO requestDTO,
//            String id, EasyCodef easyCodef) throws ExecutionException, InterruptedException {
//        CountDownLatch latch = new CountDownLatch(1); // 첫 번째 요청 완료 후 나머지 해제
//        List<CompletableFuture<String>> futures = new ArrayList<>();
//
//        for (int i = 0; i < taxInvoiceInfoList.size(); i++) {
//            final int threadIndex = i; // `i`를 final 변수로 저장
//            TaxInvoiceInfo taxInvoiceInfo = taxInvoiceInfoList.get(i);
//            HashMap<String, Object> parameterMap = createParameterMap(taxInvoiceInfo, requestDTO, id);
//
//            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
//                try {
//                    return new RequestThread(easyCodef, parameterMap, PRODUCT_URL, threadIndex, latch).call();
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            });
//
//            futures.add(future);
//        }
//
//        // 첫 번째 응답을 즉시 반환
//        try {
//            return futures.get(0).get(); // 첫 번째 스레드 응답만 가져옴 (최대 5초 대기)
//        } catch (InterruptedException | ExecutionException e) {
//            e.printStackTrace();
//            return "첫 번째 요청 처리 중 오류 발생";
//        }
//    }

//    private HashMap<String, Object> createParameterMap(TaxInvoiceInfo taxInvoiceInfo,
//            NonVerifiedTaxValidationRequestDTO requestDTO,
//            String id) {
//        HashMap<String, Object> parameterMap = new HashMap<>();
//        parameterMap.put("organization", "0004");
//        parameterMap.put("loginType", "5");
//        parameterMap.put("id", id);
//        parameterMap.put("loginTypeLevel", requestDTO.getLoginTypeLevel());
//        parameterMap.put("userName", requestDTO.getUserName());
//        parameterMap.put("phoneNo", requestDTO.getPhoneNo());
//        parameterMap.put("identity", requestDTO.getIdentity());
//        parameterMap.put("telecom", requestDTO.getTelecom());
//        parameterMap.put("supplierRegNumber", taxInvoiceInfo.getSupplierRegNumber());
//        parameterMap.put("contractorRegNumber", taxInvoiceInfo.getContractorRegNumber());
//        parameterMap.put("approvalNo", taxInvoiceInfo.getApprovalNo());
//        parameterMap.put("reportingDate", taxInvoiceInfo.getReportingDate());
//        parameterMap.put("supplyValue", taxInvoiceInfo.getSupplyValue());
//
//        return parameterMap;
//    }
}
