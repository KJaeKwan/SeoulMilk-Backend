package Seoul_Milk.sm_server.global.clovaOcr.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OcrDataExtractor {

    /**
     * 여러 개의 OCR JSON 응답을 반복 처리
     */
    public List<Map<String, String>> extractMultipleHeaderFields(List<String> jsonResponses) throws Exception {
        List<Map<String, String>> results = new ArrayList<>();
        for (String jsonResponse : jsonResponses) {
            results.add(extractHeaderFields(jsonResponse));
        }
        return results;
    }

    /**
     * OCR JSON 응답에서 전자계산서 기본 정보를 추출
     */
    public Map<String, String> extractHeaderFields(String jsonResponse) throws Exception {
        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            throw new RuntimeException("OCR JSON 응답이 비어 있습니다.");
        }

        // 깨진 따옴표 보정
        jsonResponse = fixBrokenQuotes(jsonResponse);

        // JSON 유효성 검사
        if (!isValidJson(jsonResponse)) {
            throw new RuntimeException("OCR JSON 응답이 잘못된 JSON 형식입니다.");
        }

        // 마지막 30개 필드를 제거
        jsonResponse = cleanJsonResponse(jsonResponse);

        // Jackson으로 파싱
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> root = mapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {});
        List<Map<String, Object>> images = castList(root.get("images"));

        if (images == null || images.isEmpty()) {
            throw new RuntimeException("OCR 응답 JSON에서 images 필드를 찾을 수 없습니다.");
        }
        List<Map<String, Object>> fields = castList(images.get(0).get("fields"));
        if (fields == null || fields.isEmpty()) {
            throw new RuntimeException("OCR 결과에 fields 데이터가 없습니다.");
        }

        // fields 배열에서 inferText만 추출하여 공백 제거
        List<String> textList = new ArrayList<>();
        for (Map<String, Object> field : fields) {
            String text = (String) field.get("inferText");
            if (text == null) text = "";
            text = text.replace(" ", "").trim();
            textList.add(text);
        }

        // 정규식 패턴
        Pattern pricePattern = Pattern.compile("^\\d{1,3}(,\\d{3})*$");
        Pattern datePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
        Pattern emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        Pattern approvalNumberPattern = Pattern.compile("\\d{8}-\\d{8}-\\d{8}");
        Pattern registrationNumberPattern = Pattern.compile("\\d{3}-\\d{2}-\\d{5}");

        // 키워드 매핑
        Map<String, String> keywordMap = new HashMap<>();
        keywordMap.put("승인번호", "approval_number");
        keywordMap.put("등록", "registration_number");
        keywordMap.put("공급가액", "total_amount");
        keywordMap.put("세액", "tax_amount");
        keywordMap.put("상호", "company");
        keywordMap.put("성명", "person_name");
        keywordMap.put("사업장", "business_address");
        keywordMap.put("업태", "business_type");
        keywordMap.put("종목", "product");
        keywordMap.put("이메일", "email");

        Map<String, String> extractedData = new LinkedHashMap<>();
        boolean foundIssueDate = false;
        boolean foundTotalAmount = false;
        boolean foundTaxAmount = false;

        for (int i = 0; i < textList.size(); i++) {
            String currentText = textList.get(i);

            // 승인번호, 등록번호
            if (approvalNumberPattern.matcher(currentText).matches()) {
                extractedData.put("approval_number", currentText);
            }
            if (registrationNumberPattern.matcher(currentText).matches()) {
                extractedData.put("registration_number", currentText);
            }

            // 공급가액
            if (currentText.equals("공급가액") && !foundTotalAmount) {
                for (int j = i + 1; j < textList.size(); j++) {
                    Matcher matcher = pricePattern.matcher(textList.get(j));
                    if (matcher.matches()) {
                        extractedData.put("공급가액", textList.get(j));
                        foundTotalAmount = true;
                        break;
                    }
                }
            }

            // 세액
            if (currentText.equals("세액") && !foundTaxAmount) {
                for (int j = i + 1; j < textList.size(); j++) {
                    Matcher matcher = pricePattern.matcher(textList.get(j));
                    if (matcher.matches()) {
                        extractedData.put("세액", textList.get(j));
                        foundTaxAmount = true;
                        break;
                    }
                }
            }

            // 작성일자
            Matcher dateMatcher = datePattern.matcher(currentText);
            if (dateMatcher.matches() && !foundIssueDate) {
                extractedData.put("issue_date", currentText);
                foundIssueDate = true;
            }

            // 이메일
            Matcher emailMatcher = emailPattern.matcher(currentText);
            if (emailMatcher.matches()) {
                extractedData.put("email", currentText);
            }

            // 상호, 성명, 사업장 등
            if (keywordMap.containsKey(currentText) && i + 1 < textList.size()) {
                String fieldName = keywordMap.get(currentText);
                // "번호" 같은 불필요한 단어 체크
                if (!"번호".equals(textList.get(i + 1))) {
                    extractedData.put(fieldName, textList.get(i + 1));
                }
            }
        }

        return extractedData;
    }

    /**
     * JSON 깨진 따옴표를 수정
     */
    public static String fixBrokenQuotes(String jsonResponse) {
        if (jsonResponse == null) return null;
        // "" → " 로 변환
        jsonResponse = jsonResponse.replaceAll("\"\"", "\"");
        // "조회/발급>" 등의 잘못된 이스케이프 수정
        jsonResponse = jsonResponse.replace("\"\"조회/발급>\"", "\"조회/발급>\"");
        return jsonResponse;
    }

    /**
     * Jackson으로 JSON 파싱이 가능한지 검사
     */
    public static boolean isValidJson(String json) {
        try {
            new ObjectMapper().readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * OCR 결과에서 마지막 30개 필드를 제거
     */
    public String cleanJsonResponse(String jsonResponse) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> root = mapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {});
        List<Map<String, Object>> images = castList(root.get("images"));

        if (images == null || images.isEmpty()) {
            throw new RuntimeException("OCR 응답 JSON에서 images 필드를 찾을 수 없습니다.");
        }
        Map<String, Object> firstImage = images.get(0);
        List<Map<String, Object>> fields = castList(firstImage.get("fields"));
        if (fields == null || fields.isEmpty()) {
            throw new RuntimeException("OCR 결과에 fields 데이터가 없습니다.");
        }

        int removeLastN = 30;
        if (fields.size() > removeLastN) {
            fields = fields.subList(0, fields.size() - removeLastN);
            firstImage.put("fields", fields);
        }
        // 다시 JSON 문자열로 직렬화
        root.put("images", images);
        return mapper.writeValueAsString(root);
    }

    /**
     * Object -> List<Map<String, Object>> 캐스팅 편의 메서드
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> castList(Object obj) {
        if (obj instanceof List) {
            return (List<Map<String, Object>>) obj;
        }
        return null;
    }
}
