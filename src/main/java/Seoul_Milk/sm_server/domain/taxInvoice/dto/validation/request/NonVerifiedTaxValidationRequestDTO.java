package Seoul_Milk.sm_server.domain.taxInvoice.dto.validation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;

//Todo controller이름 정해지면 description 그거에 맞추기
//Todo 삭제할 가능성 있음 배포전에 쓸일 없으면 지우기
@Schema(description = "간편인증 안한 상태에서의 진위여부 요청 api request body")
@Getter
public class NonVerifiedTaxValidationRequestDTO {
    @Schema(description = "간편인증 로그인구분(1:카카오톡, 2:페이코, 3:삼성패스, 4:KB모바일, 5:통신사(PASS), 6:네이버, 7:신한인증서, 8: toss, 9: 뱅크샐러드)")
    private String loginTypeLevel;
    @Schema(description = "사용자 이름")
    private String userName;
    @Schema(description = "휴대폰번호(01012345678형식으로)")
    private String phoneNo;
    @Schema(description = "생년월일(YYYYMMDD형식으로)")
    private String identity;
    @Schema(description = "통신사(“0\":SKT(SKT알뜰폰), “1”:KT(KT알뜰폰), “2\":LG U+(LG U+알뜰폰))")
    private String telecom;
    @Schema(description = "세금계산서 객체")
    private List<TaxInvoiceInfo> taxInvoiceInfoList;
}
