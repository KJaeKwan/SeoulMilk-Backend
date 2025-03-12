package Seoul_Milk.sm_server.global.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public class JsonUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();


    /**
     * OCR API 응답 JSON 문자열에서 inferText 값 목록을 추출
     */
    public static List<String> parseOcrResponse(String jsonResponse) throws Exception {
        // JSON 문자열을 Map으로 변환
        Map<String, Object> responseMap = objectMapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {});
        // images 배열 추출
        List<Map<String, Object>> images = (List<Map<String, Object>>) responseMap.get("images");
        if (images == null || images.isEmpty()) {
            return Collections.emptyList();
        }

        // 첫 번째 이미지의 fields 추출
        Map<String, Object> firstImage = images.get(0);
        List<Map<String, Object>> fields = (List<Map<String, Object>>) firstImage.get("fields");
        if (fields == null || fields.isEmpty()) {
            return Collections.emptyList();
        }

        // 각 field에서 inferText 값만 추출
        List<String> result = new ArrayList<>();
        for (Map<String, Object> field : fields) {
            if (field.containsKey("inferText")) {
                result.add((String) field.get("inferText"));
            }
        }
        return result;
    }
}
