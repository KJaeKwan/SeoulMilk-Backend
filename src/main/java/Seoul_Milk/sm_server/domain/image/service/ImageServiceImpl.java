package Seoul_Milk.sm_server.domain.image.service;

import Seoul_Milk.sm_server.domain.image.dto.ImageResponseDTO;
import Seoul_Milk.sm_server.domain.image.entity.Image;
import Seoul_Milk.sm_server.domain.image.repository.ImageRepository;
import Seoul_Milk.sm_server.global.exception.CustomException;
import Seoul_Milk.sm_server.global.exception.ErrorCode;
import Seoul_Milk.sm_server.global.upload.service.AwsS3Service;
import Seoul_Milk.sm_server.login.entity.MemberEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final AwsS3Service awsS3Service;

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
    public void markAsTemporary(List<MultipartFile> files, MemberEntity member) {
        List<String> imageUrls = awsS3Service.uploadFiles("temporary-images", files, true);

        // 이미지 엔티티 생성
        List<Image> images = imageUrls.stream()
                .map(url -> {
                    Image image = Image.create(url, member);
                    image.markAsTemporary(); // 임시 저장 상태로 변경
                    return image;
                })
                .collect(Collectors.toList());

        // DB 저장
        imageRepository.saveAll(images);
    }

    /**
     * 리스트로 임시 저장 해제
     */
    @Override
    @Transactional
    public void removeFromTemporary(MemberEntity member) {
        List<Image> images = imageRepository.findAllByMember(member);

        if (images.isEmpty()) {
            throw new CustomException(ErrorCode.TMP_IMAGE_NOT_EXIST);
        }

        images.forEach(image -> awsS3Service.deleteFile(image.getImageUrl()));
        images.forEach(Image::removeFromTemporary);
        imageRepository.saveAll(images);
    }


    @Override
    public List<String> getTempImageUrlsForOcr(MemberEntity member) {
        List<Image> images = imageRepository.findAllByMember(member);

        if (images.isEmpty()) {
            throw new CustomException(ErrorCode.TMP_IMAGE_NOT_EXIST);
        }

        // URL 리스트 반환
        return images.stream()
                .map(Image::getImageUrl)
                .toList();
    }

}
