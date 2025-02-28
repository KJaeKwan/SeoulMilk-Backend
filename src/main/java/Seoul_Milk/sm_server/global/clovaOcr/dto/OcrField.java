package Seoul_Milk.sm_server.global.clovaOcr.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OcrField {
    private String inferText;
    private BoundingPoly boundingPoly;
}
