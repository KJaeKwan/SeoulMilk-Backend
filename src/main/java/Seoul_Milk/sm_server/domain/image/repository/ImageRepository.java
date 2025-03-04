package Seoul_Milk.sm_server.domain.image.repository;

import Seoul_Milk.sm_server.domain.image.entity.Image;
import Seoul_Milk.sm_server.login.entity.MemberEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ImageRepository {
    Page<Image> searchTempImages(MemberEntity member, Pageable pageable);
    void saveAll(List<Image> images);
    List<Image> findAllByMember(MemberEntity member);
}
