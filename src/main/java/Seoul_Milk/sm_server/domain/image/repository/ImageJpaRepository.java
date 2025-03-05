package Seoul_Milk.sm_server.domain.image.repository;

import Seoul_Milk.sm_server.domain.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageJpaRepository extends JpaRepository<Image, Long> {
}
