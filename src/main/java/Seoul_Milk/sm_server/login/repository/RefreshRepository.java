package Seoul_Milk.sm_server.login.repository;


import Seoul_Milk.sm_server.login.entity.RefreshEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshRepository extends JpaRepository<RefreshEntity, Long> {

}
