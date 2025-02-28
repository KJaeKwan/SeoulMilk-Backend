package Seoul_Milk.sm_server.domain.taxValidation.thread;

import Seoul_Milk.sm_server.domain.taxValidation.shared.SharedData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.codef.api.EasyCodef;
import io.codef.api.EasyCodefServiceType;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class RequestThread extends Thread {
    private EasyCodef codef;
    private HashMap<String, Object> parameterMap;
    private int threadNo;
    private String productUrl;

    public RequestThread(EasyCodef codef, HashMap<String, Object> parameterMap, int threadNo, String productUrl) {
        this.codef = codef;
        this.parameterMap = parameterMap;
        this.threadNo = threadNo;
        this.productUrl = productUrl;
    }

    @Override
    public void run() {

        String result;
        String code;
        boolean continue2Way = false;

        try {
            result = codef.requestProduct(productUrl, EasyCodefServiceType.DEMO, parameterMap);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        HashMap<String, Object> responseMap = null;
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
            continue2Way = Boolean.valueOf((boolean)dataMap.get("continue2Way"));
        }

        // 응답코드가 CF-03002 이고 continue2Way 필드가 true인 경우 추가 인증 정보를 변수에 저장
        if (code.equals("CF-03002") && continue2Way){
            SharedData.JOB_INDEX =  (int)dataMap.get("jobIndex");
            SharedData.THREAD_INDEX = (int)dataMap.get("threadIndex");
            SharedData.JTI = (String) dataMap.get("jti");
            SharedData.TWO_WAY_TIMESTAMP = (Long)dataMap.get("twoWayTimestamp");
        }

        /** #8.결과값 확인 */
        System.out.println("threadNo " + threadNo + " result : " + result);
    }
}
