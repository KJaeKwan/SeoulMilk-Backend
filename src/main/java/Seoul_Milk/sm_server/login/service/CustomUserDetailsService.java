package Seoul_Milk.sm_server.login.service;

import Seoul_Milk.sm_server.login.dto.CustomUserDetails;
import Seoul_Milk.sm_server.login.entity.MemberEntity;
import Seoul_Milk.sm_server.login.repository.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberJpaRepository memberJpaRepository;
    @Override
    public UserDetails loadUserByUsername(String employeeId) throws UsernameNotFoundException {
        MemberEntity userData = memberJpaRepository.findByEmployeeId(employeeId);
        if(userData != null){
            return new CustomUserDetails(userData);
        }
        return null;
    }
}
