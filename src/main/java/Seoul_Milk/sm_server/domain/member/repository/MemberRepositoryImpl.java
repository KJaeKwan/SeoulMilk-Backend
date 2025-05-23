package Seoul_Milk.sm_server.domain.member.repository;

import Seoul_Milk.sm_server.global.common.exception.CustomException;
import Seoul_Milk.sm_server.global.common.exception.ErrorCode;
import Seoul_Milk.sm_server.domain.member.entity.MemberEntity;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository{

    private final MemberJpaRepository memberJpaRepository;

    @Override
    public Optional<MemberEntity> findById(Long id) {
        return memberJpaRepository.findById(id);
    }

    @Override
    public MemberEntity getById(Long id) {
        return findById(id).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_EXIST));
    }

    @Override
    public Optional<MemberEntity> findByEmployeeId(String employeeId) {
        return memberJpaRepository.findByEmployeeId(employeeId);
    }

    @Override
    public MemberEntity getByEmployeeId(String employeeId) {
        return findByEmployeeId(employeeId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_EXIST));
    }

    @Override
    public Optional<MemberEntity> findByEmail(String email) {
        return memberJpaRepository.findByEmail(email);
    }

    @Override
    public Boolean existsByEmployeeId(String employeeId) {
        return memberJpaRepository.existsByEmployeeId(employeeId);
    }

    @Override
    public MemberEntity save(MemberEntity memberEntity) {
        return memberJpaRepository.save(memberEntity);
    }
}
