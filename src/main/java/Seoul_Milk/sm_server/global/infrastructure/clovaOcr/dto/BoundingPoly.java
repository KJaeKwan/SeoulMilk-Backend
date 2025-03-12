package Seoul_Milk.sm_server.global.infrastructure.clovaOcr.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoundingPoly {
    private List<Vertex> vertices;
}
