package Seoul_Milk.sm_server.domain.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "임시저장 이미지 관련 요청 DTO")
public class ImageRequestDTO {

    @Schema(description = "선택한 임시저장 이미지 해제 요청 DTO")
    public record RemoveTemporary(
            @Schema(description = "삭제할 이미지 ID 리스트") List<Long> imageIds
    ) {
        public static RemoveTemporary from(List<Long> imageIds) {
            return new RemoveTemporary(imageIds);
        }
    }
}
