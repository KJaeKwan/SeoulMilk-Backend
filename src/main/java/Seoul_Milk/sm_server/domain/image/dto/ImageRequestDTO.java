package Seoul_Milk.sm_server.domain.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "임시저장 이미지 관련 요청 DTO")
public class ImageRequestDTO {

    @Schema(description = "선택한 임시저장 이미지 해제 요청 DTO")
    public record RemoveTemporary(
            @Schema(description = "삭제할 이미지 ID 리스트") List<Long> imageIds
    ) {}

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
