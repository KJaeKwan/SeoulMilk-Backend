package Seoul_Milk.sm_server.mock.repository;

import Seoul_Milk.sm_server.domain.image.entity.Image;
import Seoul_Milk.sm_server.domain.image.repository.ImageRepository;
import Seoul_Milk.sm_server.domain.member.entity.MemberEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class FakeImageRepository implements ImageRepository {

    @Override
    public Page<Image> searchTempImages(MemberEntity member, Pageable pageable) {
        return null;
    }

    @Override
    public void saveAll(List<Image> images) {

    }

    @Override
    public List<Image> findByMemberAndIds(MemberEntity member, List<Long> imageIds) {
        return null;
    }
}
