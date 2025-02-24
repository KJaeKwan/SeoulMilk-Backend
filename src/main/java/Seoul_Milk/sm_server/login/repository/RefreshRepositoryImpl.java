package Seoul_Milk.sm_server.login.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RefreshRepositoryImpl implements RefreshRepository{
    private final RefreshJpaRepository refreshJpaRepository;

    @Override
    public Boolean existsByRefreshToken(String refreshToken) {
        return refreshJpaRepository.existsByRefreshToken(refreshToken);
    }

    @Override
    public void deleteByRefreshToken(String refreshToken) {
        refreshJpaRepository.deleteByRefreshToken(refreshToken);
    }
}
