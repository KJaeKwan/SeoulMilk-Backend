package Seoul_Milk.sm_server.global.auth.service;

import Seoul_Milk.sm_server.global.exception.ErrorCode;
import Seoul_Milk.sm_server.global.auth.dto.CustomUserDetails;
import Seoul_Milk.sm_server.domain.member.entity.MemberEntity;
import Seoul_Milk.sm_server.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;
    @Override
    public UserDetails loadUserByUsername(String employeeId) throws UsernameNotFoundException {
        MemberEntity userData = memberRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new UsernameNotFoundException(ErrorCode.USER_EMPLOYEE_ID_NOT_EXIST.getMessage()));
        if(userData != null){
            return new CustomUserDetails(userData);
        }
        return null;
    }
}
