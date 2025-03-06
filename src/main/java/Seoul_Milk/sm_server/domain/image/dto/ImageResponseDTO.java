package Seoul_Milk.sm_server.domain.image.dto;

import Seoul_Milk.sm_server.domain.image.entity.Image;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "임시저장 이미지 관련 응답 DTO")
public class ImageResponseDTO {

    @Schema(description = "임시저장 이미지 단건 조회 응답 DTO")
    public record GetOne(
            @Schema(description = "이미지 ID") Long imageId,
            @Schema(description = "이미지 URL") String imageUrl,
            @Schema(description = "날짜") LocalDate uploadDate
    ) {
        public static GetOne from(Image image) {
            return new GetOne(
                    image.getId(),
                    image.getImageUrl(),
                    image.getUploadDate()
            );
        }
    }
}
