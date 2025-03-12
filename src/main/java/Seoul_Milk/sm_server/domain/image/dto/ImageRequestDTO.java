package Seoul_Milk.sm_server.domain.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "임시저장 이미지 관련 요청 DTO")
public class ImageRequestDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "새로운 이미지 저장 요청 DTO")
    public static class SaveImage {
        @Schema(description = "승인번호", nullable = true)
        private String issueId;

        @Schema(description = "공급자 등록번호", nullable = true)
        private String ipId;

        @Schema(description = "공급받는자 등록번호", nullable = true)
        private String suId;

        @Schema(description = "공급가액", nullable = true)
        private Integer chargeTotal;

        @Schema(description = "작성일자", nullable = true)
        private String erDat;
    }
}
