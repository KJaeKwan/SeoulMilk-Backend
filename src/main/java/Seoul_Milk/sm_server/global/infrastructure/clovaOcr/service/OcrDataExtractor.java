package Seoul_Milk.sm_server.global.infrastructure.clovaOcr.service;

import Seoul_Milk.sm_server.global.infrastructure.clovaOcr.dto.OcrField;
import Seoul_Milk.sm_server.global.infrastructure.clovaOcr.dto.TemplateOcrField;
import Seoul_Milk.sm_server.global.infrastructure.clovaOcr.dto.Vertex;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class OcrDataExtractor {

    /**
     * Template OCR에서 필요한 데이터를 추출하는 메서드
     */
    public Map<String, Object> extractDataFromTemplateOcrFields(List<TemplateOcrField> ocrFields) {
        Map<String, Object> extractedData = new LinkedHashMap<>();

        for (TemplateOcrField field : ocrFields) {
            String fieldName = field.getName();
            String fieldValue = field.getInferText().replaceAll("\\n", "").trim();

            if (fieldName == null || fieldValue.isEmpty()) {
                continue;
            }

            switch (fieldName) {
                case "issueId":
                    extractedData.put("approval_number", fieldValue);
                    break;
                case "ipId":
                    extractedData.put("supplier_registration_number", fieldValue);
                    break;
                case "suId":
                    extractedData.put("recipient_registration_number", fieldValue);
                    break;
                case "erDat":
                    extractedData.put("issue_date", fieldValue);
                    break;
                case "chargeTotal":
                    extractedData.put("chargeTotal", fieldValue);
                    break;
                case "taxTotal":
                    extractedData.put("total_amount", fieldValue);
                    break;
                case "grandTotal":
                    extractedData.put("grandTotal", fieldValue);
                    break;
                case "ipBusinessName":
                    extractedData.put("supplier_business_name", fieldValue);
                    break;
                case "suBusinessName":
                    extractedData.put("recipient_business_name", fieldValue);
                    break;
                case "ipName":
                    extractedData.put("supplier_name", fieldValue);
                    break;
                case "suName":
                    extractedData.put("recipient_name", fieldValue);
                    break;
                case "ipAddress":
                    extractedData.put("supplier_address", fieldValue);
                    break;
                case "suAddress":
                    extractedData.put("recipient_address", fieldValue);
                    break;
                case "ipEmail":
                    extractedData.put("supplier_email", fieldValue);
                    break;
                case "suEmail":
                    extractedData.put("recipient_email", fieldValue);
                    break;
            }
        }

        return extractedData;
    }



    /**
     * General OCR에서 필요한 데이터를 추출하는 메서드
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
            }
        }

        extractedData.put("registration_numbers", registrationNumbers);
        System.out.println("registrationNumbers = " + registrationNumbers);

        // 상호명 추출
        Map<String, String> businessNames = extractBusinessNames(ocrFields, registrationNumbers);
        if (!businessNames.isEmpty()) {
         extractedData.putAll(businessNames);
        }

        // 성명 정보 추출
         List<String> names = extractNamesInOrder(ocrFields);
         extractedData.put("supplier_name", !names.isEmpty() ? names.get(0) : null);
         extractedData.put("recipient_name", names.size() > 1 ? names.get(1) : null);

        return extractedData;
    }


    /**
     * 상대 위치를 기반으로 상호명 추출하는 메서드
     */
    private Map<String, String> extractBusinessNames(List<OcrField> ocrFields, List<String> registrationNumbers) {
        Map<String, String> businessNames = new LinkedHashMap<>();

        int regCount = registrationNumbers.size();
        for (int i = 0; i < regCount; i++) {
            String regNumber = registrationNumbers.get(i);
            String key = (i == 0) ? "supplier_business_name" : "recipient_business_name";

            // 개별 등록번호 찾기
            OcrField regField = findRegistrationField(ocrFields, regNumber);

            // 등록번호별 개별 "상호", "성명", "사업장" 찾기
            OcrField businessNameField = findNearestFieldByXAndY(ocrFields, regField, "상호");
            OcrField nameField = findNearestFieldByXAndY(ocrFields, regField, "성명");
            OcrField businessField = findNearestFieldByXAndY(ocrFields, regField, "사업장");

            // 각 등록번호별 기준 좌표
            int regBottomY = getBottomY(regField);
            int refCenterX = getCenterX(regField);
            int sanghoX = getCenterX(businessNameField);
            int seongmyeongX = getCenterX(nameField);
            int businessY = getCenterY(businessField);

            // 개별 등록번호의 상호명을 필터링
            List<OcrField> businessNameFields = ocrFields.stream()
                    .filter(field -> field.getBoundingPoly() != null)
                    .filter(field -> getCenterY(field) > regBottomY) // 해당 등록번호의 최하단 Y 값보다 아래에 있는 값만 포함
                    .filter(field -> getCenterX(field) > sanghoX) // 해당 등록번호의 "상호"보다 오른쪽
                    .filter(field -> getCenterX(field) < seongmyeongX) // 해당 등록번호의 "성명"보다 왼쪽
                    .filter(field -> getCenterY(field) < businessY) // 해당 등록번호의 "사업장"보다 위쪽
                    .filter(field -> Math.abs(getCenterX(field) - refCenterX) < 100) // 등록번호의 X와 크게 차이나지 않는 필드만 포함
                    .filter(field -> !field.getInferText().matches("번호|설명|상호|성명|법인형")) // 불필요한 데이터 필터링
                    .sorted(Comparator.comparingInt(this::getCenterX))
                    .toList();

            // 상호명 합치기
            String businessName = businessNameFields.stream()
                    .sorted(Comparator.comparingInt(this::getCenterX).reversed())
                    .map(OcrField::getInferText)
                    .collect(Collectors.joining());

            businessNames.put(key, businessName);
        }

        return businessNames;
    }

    /** OCR 데이터에서 "성명" 값을 추출하는 메서드 */
    private List<String> extractNamesInOrder(List<OcrField> ocrFields) {
        List<String> nameKeywords = Arrays.asList("성명", "설명");
        List<String> names = new ArrayList<>();

        for (int i = 0; i < ocrFields.size(); i++) {
            String currentText = ocrFields.get(i).getInferText().trim();

            if (nameKeywords.contains(currentText) && i + 1 < ocrFields.size()) {
                names.add(ocrFields.get(i + 1).getInferText().trim());
            }

            if (names.size() == 2) break;
        }

        return names;
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

    /** 등록번호를 찾는 메서드 */
    private OcrField findRegistrationField(List<OcrField> ocrFields, String registrationNumber) {
        return ocrFields.stream()
                .filter(field -> field.getInferText().equals(registrationNumber))
                .findFirst()
                .orElse(null);
    }


    /** 등록번호와 가까운 값 찾기 */
    private OcrField findNearestFieldByXAndY(List<OcrField> ocrFields, OcrField referenceField, String targetText) {
        int refCenterX = getCenterX(referenceField);
        int refBottomY = getBottomY(referenceField);

        return ocrFields.stream()
                .filter(field -> isMatchingText(field.getInferText(), targetText)) // 유사한 텍스트도 고려
                .filter(field -> getCenterY(field) > refBottomY) // 등록번호 아래에 있는 필드만 포함
                .min(Comparator.comparingInt(field -> Math.abs(getCenterX(field) - refCenterX))) // X 거리 차이가 가장 작은 값 선택
                .orElse(null);
    }

    private boolean isMatchingText(String actualText, String expectedText) {
        if (actualText.equals(expectedText)) {
            return true; // 정확히 일치하면 true
        }

        // OCR 인식 오류 보정
        Map<String, List<String>> similarWords = new HashMap<>();
        similarWords.put("성명", Arrays.asList("설명", "성면"));
        similarWords.put("상호", Arrays.asList("상오", "상효"));

        return similarWords.getOrDefault(expectedText, Collections.emptyList()).contains(actualText);
    }


    private int getCenterX(OcrField field) {
        List<Vertex> vertices = field.getBoundingPoly().getVertices();
        return (vertices.get(0).getX() + vertices.get(2).getX()) / 2;
    }

    private int getCenterY(OcrField field) {
        List<Vertex> vertices = field.getBoundingPoly().getVertices();
        return (vertices.get(0).getY() + vertices.get(2).getY()) / 2;
    }

    private int getBottomY(OcrField field) {
        List<Vertex> vertices = field.getBoundingPoly().getVertices();
        return vertices.stream().mapToInt(Vertex::getY).max().orElse(0);
    }
}
