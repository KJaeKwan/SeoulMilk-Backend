package Seoul_Milk.sm_server.domain.image.controller;

import Seoul_Milk.sm_server.domain.image.dto.ImageRequestDTO;
import Seoul_Milk.sm_server.domain.image.dto.ImageResponseDTO;
import Seoul_Milk.sm_server.domain.image.service.ImageService;
import Seoul_Milk.sm_server.global.annotation.CurrentMember;
import Seoul_Milk.sm_server.global.dto.response.SuccessResponse;
import Seoul_Milk.sm_server.login.entity.MemberEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/image/tmp")
@RequiredArgsConstructor
@Tag(name = "이미지 임시 저장 API")
public class ImageController {

    private final ImageService imageService;

    /**
     * 특정 멤버의 임시 저장된 이미지 전체 조회
     * @param member 로그인 유저
     * @return 임시 저장된 이미지 리스트
     */
    @Operation(summary = "임시 저장된 이미지 전체 조회 (페이지네이션)")
    @GetMapping
    public SuccessResponse<Page<ImageResponseDTO.GetOne>> getAllTemporary(
            @CurrentMember MemberEntity member,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<ImageResponseDTO.GetOne> result = imageService.getAll(member, page-1, size);
        return SuccessResponse.ok(result);
    }

    /**
     * 특정 ID 리스트를 찾아 임시 저장 등록
     * @param files 임시 저장할 이미지 리스트
     * @param member 로그인 유저
     * @return 성공 메시지
     */
    @Operation(summary = "여러 개의 이미지를 임시 저장 상태로 등록")
    @PostMapping(value = "/mark", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessResponse<String> markAsTemporary(
            @RequestPart(value = "requests", required = false) String requestsJson,
            @RequestPart(value = "files") List<MultipartFile> files,
            @CurrentMember MemberEntity member) throws JsonProcessingException {

        if (requestsJson == null || requestsJson.trim().isEmpty()) {
            requestsJson = "[]";
        }

        ObjectMapper objectMapper = new ObjectMapper();
        List<ImageRequestDTO.SaveImage> requests = objectMapper.readValue(
                requestsJson, new TypeReference<List<ImageRequestDTO.SaveImage>>() {}
        );

        imageService.markAsTemporary(requests, files, member);
        return SuccessResponse.ok("선택한 이미지들이 임시 저장으로 설정되었습니다.");
    }

    /**
     * 해당 유저의 모든 임시 저장 이미지를 해제
     * @param member 로그인 유저
     * @return 성공 메시지
     */
    @Operation(summary = "리스트(ID)로 받은 임시 저장 이미지를 해제")
    @PatchMapping("/unmark")
    public SuccessResponse<String> removeFromTemporary(
            @CurrentMember MemberEntity member,
            @RequestBody ImageRequestDTO.RemoveTemporary request
    ) {
        imageService.removeFromTemporary(member, request.imageIds());
        return SuccessResponse.ok("모든 임시 저장 이미지가 해제되었습니다.");
    }
}
