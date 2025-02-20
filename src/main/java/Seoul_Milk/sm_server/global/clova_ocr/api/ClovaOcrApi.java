package Seoul_Milk.sm_server.global.clova_ocr.api;

import Seoul_Milk.sm_server.global.clova_ocr.util.JsonUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

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
     * MultipartFile을 받아 OCR API 요청
     * @param type HTTP 메서드 (POST)
     * @param file 업로드된 Multipart 이미지 파일
     * @param naverSecretKey 네이버 클로바 OCR Secret Key
     * @param contentType 파일 확장자 (예: image/png)
     */
    public List<String> callApi(String type, MultipartFile file, String naverSecretKey, String contentType) {
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
            image.put("format", contentType.replace("image/", ""));
            image.put("name", file.getOriginalFilename());

            JSONArray images = new JSONArray();
            images.add(image);
            json.put("images", images);
            String postParams = json.toJSONString();

            // MultipartFile API로 전송
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
            e.printStackTrace();
        }
        return parseData;
    }

    /**
     * Multipart 데이터 전송
     */
    private static void writeMultiPart(OutputStream out, String jsonMessage, MultipartFile file, String boundary) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"message\"\r\n\r\n");
        sb.append(jsonMessage).append("\r\n");

        out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        out.flush();

        if (!file.isEmpty()) {
            try (InputStream fis = file.getInputStream()) {
                StringBuilder fileString = new StringBuilder();
                fileString.append("--").append(boundary).append("\r\n")
                        .append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                        .append(file.getOriginalFilename()).append("\"\r\n")
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
     * CLOVA OCR API에서 받은 JSON 응답을 파싱
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
