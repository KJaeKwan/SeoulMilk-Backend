package Seoul_Milk.sm_server.global.clovaOcr.service;

import Seoul_Milk.sm_server.global.clovaOcr.dto.OcrField;
import Seoul_Milk.sm_server.global.exception.CustomException;
import Seoul_Milk.sm_server.global.exception.ErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class OcrDataExtractor {

    /**
     * OCR JSON 응답에서 전자계산서 기본 정보를 추출
     */
    public List<OcrField> extractOcrFields(String jsonResponse) throws Exception {
        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            throw new CustomException(ErrorCode.OCR_EMPTY_JSON);
        }

        // 깨진 따옴표 보정
        jsonResponse = fixBrokenQuotes(jsonResponse);

        // JSON 유효성 검사
        if (!isValidJson(jsonResponse)) {
            throw new CustomException(ErrorCode.OCR_INVALID_JSON);
        }

        jsonResponse = cleanJsonResponse(jsonResponse);

        // Jackson으로 파싱
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> root = mapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {});
        List<Map<String, Object>> images = castList(root.get("images"));

        if (images == null || images.isEmpty()) {
            throw new CustomException(ErrorCode.OCR_NO_IMAGES);
        }

        List<Map<String, Object>> fields = castList(images.get(0).get("fields"));
        if (fields == null || fields.isEmpty()) {
            throw new CustomException(ErrorCode.OCR_NO_FIELDS);
        }

        return fields.stream()
                .map(field -> mapper.convertValue(field, OcrField.class))  // JSON을 OcrField DTO로 변환
                .collect(Collectors.toList());
    }

    /**
     * OCR에서 필요한 데이터를 추출하는 메서드
     */
     public Map<String, Object> extractDataFromOcrFields(List<OcrField> ocrFields) {
        Map<String, Object> extractedData = new LinkedHashMap<>();
        List<String> registrationNumbers = new ArrayList<>();

        Pattern datePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
        Pattern emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        Pattern approvalNumberPattern = Pattern.compile("\\d{8}-\\d{8}-\\d{8}");
        Pattern registrationNumberPattern = Pattern.compile("\\d{3}-\\d{2}-\\d{5}");

        boolean foundIssueDate = false;
        boolean foundTotalAmount = false;
        boolean foundTaxAmount = false;

        for (int i = 0; i < ocrFields.size(); i++) {
            OcrField currentField = ocrFields.get(i);
            String currentText = currentField.getInferText().replace(" ", "").trim();

            // 승인번호
            if (approvalNumberPattern.matcher(currentText).matches()) {
                extractedData.put("approval_number", currentText);
                continue;
            }

            // 등록번호
            if (registrationNumberPattern.matcher(currentText).matches()) {
                registrationNumbers.add(currentText);
                continue;
            }

            // 작성일자
            Matcher dateMatcher = datePattern.matcher(currentText);
            if (dateMatcher.matches() && !foundIssueDate) {
                extractedData.put("issue_date", currentText);
                foundIssueDate = true;
                continue;
            }

            // 이메일
            Matcher emailMatcher = emailPattern.matcher(currentText);
            if (emailMatcher.matches()) {
                extractedData.put("email", currentText);
                continue;
            }

            // 공급가액
            if (currentText.equals("공급가액") && !foundTotalAmount) {
                for (int j = i + 1; j < ocrFields.size(); j++) {
                    if (isValidPrice(ocrFields.get(j))) {
                        extractedData.put("total_amount", ocrFields.get(j).getInferText());
                        foundTotalAmount = true;
                        break;
                    }
                }
                continue;
            }

            // 세액
            if (currentText.equals("세액") && !foundTaxAmount) {
                for (int j = i + 1; j < ocrFields.size(); j++) {
                    if (isValidPrice(ocrFields.get(j))) {
                        extractedData.put("tax_amount", ocrFields.get(j).getInferText());
                        foundTaxAmount = true;
                        break;
                    }
                }
                continue;
            }
        }

        if (!registrationNumbers.isEmpty()) {
            extractedData.put("registration_numbers", registrationNumbers);
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
            throw new CustomException(ErrorCode.OCR_NO_IMAGES);
        }

        Map<String, Object> firstImage = images.get(0);
        List<Map<String, Object>> fields = castList(firstImage.get("fields"));
        if (fields == null || fields.isEmpty()) {
            throw new CustomException(ErrorCode.OCR_NO_FIELDS);
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

    /**
     * 금액 검증 메서드 - 금액이 아닌 값이 들어오는 것을 방지
     */
    private boolean isValidPrice(OcrField field) {
        if (field == null || field.getInferText() == null) {
            return false;
        }

        String priceText = field.getInferText().replaceAll(",", "").trim();

        // 숫자로만 이루어졌는지 검사
        if (!priceText.matches("^\\d+$")) {
            return false;
        }

        // 숫자로 변환 후 최소 10,000 이상인지 확인
        try {
            int value = Integer.parseInt(priceText);
            return value >= 10000;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
