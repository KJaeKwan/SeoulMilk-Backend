package Seoul_Milk.sm_server.domain.image.service;

import Seoul_Milk.sm_server.domain.image.dto.ImageRequestDTO;
import Seoul_Milk.sm_server.domain.image.dto.ImageResponseDTO;
import Seoul_Milk.sm_server.domain.image.entity.Image;
import Seoul_Milk.sm_server.domain.image.repository.ImageRepository;
import Seoul_Milk.sm_server.global.exception.CustomException;
import Seoul_Milk.sm_server.global.exception.ErrorCode;
import Seoul_Milk.sm_server.global.upload.service.AwsS3Service;
import Seoul_Milk.sm_server.domain.member.entity.MemberEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final AwsS3Service awsS3Service;
    private final ObjectMapper objectMapper;

    /**
     * 임시 저장된 이미지 전체 조회
     * isTemporary가 true인 모든 데이터를 반환
     */
    @Override
    public Page<ImageResponseDTO.GetOne> getAll(MemberEntity member, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return imageRepository.searchTempImages(member, pageable)
                .map(ImageResponseDTO.GetOne::from);
    }

    /**
     * 리스트로 임시 저장 등록
     */
    @Override
    @Transactional
    public void markAsTemporary(String requestsJson, List<MultipartFile> files, MemberEntity member) throws JsonProcessingException {
        List<ImageRequestDTO.SaveImage> requests = parseRequests(requestsJson);
        List<String> imageUrls = awsS3Service.uploadFiles("temporary-images", files, true);

        List<Image> images = IntStream.range(0, files.size())
                .mapToObj(i -> createImage(imageUrls.get(i), getDtoAt(requests, i), member))
                .toList();

        imageRepository.saveAll(images);
    }

    /**
     * 리스트로 임시 저장 해제
     */
    @Override
    @Transactional
    public void removeFromTemporary(MemberEntity member, List<Long> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) {
            throw new CustomException(ErrorCode.TMP_IMAGE_NOT_EXIST);
        }

        List<Image> images = imageRepository.findByMemberAndIds(member, imageIds);

        if (images.size() != imageIds.size()) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        images.forEach(image -> awsS3Service.deleteFile(image.getImageUrl()));
        images.forEach(Image::removeFromTemporary);
        imageRepository.saveAll(images);
    }

    @Override
    public List<Image> getTempImagesByIds(MemberEntity member, List<Long> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) {
            throw new CustomException(ErrorCode.TMP_IMAGE_NOT_EXIST);
        }

        List<Image> images = imageRepository.findByMemberAndIds(member, imageIds);

        if (images.isEmpty()) {
            throw new CustomException(ErrorCode.TMP_IMAGE_NOT_EXIST);
        }

        return images;
    }


    // String으로 받은 요청을 SaveImage 리스트로 변환
    private List<ImageRequestDTO.SaveImage> parseRequests(String requestsJson) throws JsonProcessingException {
        if (requestsJson == null || requestsJson.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return objectMapper.readValue(requestsJson, new TypeReference<List<ImageRequestDTO.SaveImage>>() {});
    }

    // 요청 리스트에서 지정된 index에 해당하는 DTO를 반환
    private ImageRequestDTO.SaveImage getDtoAt(List<ImageRequestDTO.SaveImage> requests, int index) {
        return (requests != null && index < requests.size()) ? requests.get(index) : null;
    }

    // Image 객체를 생성
    private Image createImage(String imageUrl, ImageRequestDTO.SaveImage dto, MemberEntity member) {
        return Image.builder()
                .imageUrl(imageUrl)
                .temporary(true)
                .uploadDate(LocalDate.now())
                .issueId(dto != null ? dto.getIssueId() : null)
                .ipId(dto != null ? dto.getIpId() : null)
                .suId(dto != null ? dto.getSuId() : null)
                .chargeTotal(dto != null ? dto.getChargeTotal() : 0)
                .erDat(dto != null ? dto.getErDat() : null)
                .member(member)
                .build();
    }
}
