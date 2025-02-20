package Seoul_Milk.sm_server.global.clova_ocr.api;

import Seoul_Milk.sm_server.global.clova_ocr.util.JsonUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class ClovaOcrApi {

    private final String clovaOcrUrl;
    public ClovaOcrApi(@Value("${clova.ocr.url}") String clovaOcrUrl) {
        this.clovaOcrUrl = clovaOcrUrl;
    }

    /**
     * CLOVA OCR API 호출
     * @param type 호출 메서드 타입
     * @param filePath 파일 경로
     * @param naverSecretKey 네이버 시크릿키 값
     * @param ext 확장자
     * @return 추출된 텍스트 리스트
     */
    public List<String> callApi(String type, String filePath, String naverSecretKey, String ext) {
        String apiURL = clovaOcrUrl;
        List<String> parseData = null;

        try {
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setUseCaches(false);
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setReadTimeout(30000);
            con.setRequestMethod(type);

            String boundary = "----" + UUID.randomUUID().toString().replaceAll("-", "");
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            con.setRequestProperty("X-OCR-SECRET", naverSecretKey);

            // JSON 요청 데이터 구성
            JSONObject json = new JSONObject();
            json.put("version", "V2");
            json.put("requestId", UUID.randomUUID().toString());
            json.put("timestamp", System.currentTimeMillis());

            JSONObject image = new JSONObject();
            image.put("format", ext);
            image.put("name", "demo");

            JSONArray images = new JSONArray();
            images.add(image);
            json.put("images", images);
            String postParams = json.toJSONString();

            // 요청 전송
            File file = new File(filePath);
            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                writeMultiPart(wr, postParams, file, boundary);
            }

            // 응답 처리
            int responseCode = con.getResponseCode();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    responseCode == 200 ? con.getInputStream() : con.getErrorStream(), StandardCharsets.UTF_8))) {

                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                parseData = jsonparse(response.toString());
            }

        } catch (Exception e) {
            e.printStackTrace(); // Logger 활용 가능
        }
        return parseData;
    }

    /**
     * Multipart 데이터 전송
     * @param out 데이터 출력 스트림
     * @param jsonMessage JSON 데이터
     * @param file 전송할 파일
     * @param boundary Multipart Boundary
     */
    private static void writeMultiPart(OutputStream out, String jsonMessage, File file, String boundary) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"message\"\r\n\r\n");
        sb.append(jsonMessage).append("\r\n");

        out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        out.flush();

        if (file != null && file.isFile()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                StringBuilder fileString = new StringBuilder();
                fileString.append("--").append(boundary).append("\r\n")
                        .append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                        .append(file.getName()).append("\"\r\n")
                        .append("Content-Type: application/octet-stream\r\n\r\n");

                out.write(fileString.toString().getBytes(StandardCharsets.UTF_8));
                byte[] buffer = new byte[8192];
                int count;
                while ((count = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                }
                out.write("\r\n".getBytes());
                out.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
            }
        }
        out.flush();
    }

    /**
     * JSON 응답 데이터 가공
     * @param response 응답 문자열
     * @return 추출된 텍스트 리스트
     * @throws ParseException JSON 파싱 오류
     */
    private static List<String> jsonparse(String response) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject jobj = (JSONObject) parser.parse(response);

        // images 배열 확인
        JSONArray imagesArray = (JSONArray) jobj.get("images");
        if (imagesArray == null || imagesArray.isEmpty()) {
            return Collections.emptyList();
        }

        JSONObject firstImage = (JSONObject) imagesArray.get(0);
        JSONArray fieldsArray = (JSONArray) firstImage.get("fields");
        if (fieldsArray == null || fieldsArray.isEmpty()) {
            return Collections.emptyList();
        }

        // fields 배열에서 inferText 추출
        List<Map<String, Object>> extractedFields = JsonUtil.getListMapFromJsonArray(fieldsArray);
        List<String> result = new ArrayList<>();
        for (Map<String, Object> field : extractedFields) {
            if (field.containsKey("inferText")) {
                result.add((String) field.get("inferText"));
            }
        }
        return result;
    }
}
