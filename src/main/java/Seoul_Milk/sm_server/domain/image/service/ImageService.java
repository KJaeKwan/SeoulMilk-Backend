package Seoul_Milk.sm_server.domain.image.service;

import Seoul_Milk.sm_server.domain.image.dto.ImageRequestDTO;
import Seoul_Milk.sm_server.domain.image.dto.ImageResponseDTO;
import Seoul_Milk.sm_server.domain.image.entity.Image;
import Seoul_Milk.sm_server.login.entity.MemberEntity;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {
    Page<ImageResponseDTO.GetOne> getAll(MemberEntity member, int page, int size);
    void markAsTemporary(List<ImageRequestDTO.SaveImage> requests, List<MultipartFile> files, MemberEntity member);
    void removeFromTemporary(MemberEntity member, List<Long> imageIds);
    List<Image> getTempImagesByIds(MemberEntity member, List<Long> imageIds);
}
