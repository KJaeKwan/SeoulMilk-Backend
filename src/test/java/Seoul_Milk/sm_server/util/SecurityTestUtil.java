package Seoul_Milk.sm_server.util;

import Seoul_Milk.sm_server.domain.member.entity.MemberEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

public class SecurityTestUtil {

    public static void setAuthentication(MemberEntity member) {
        UserDetails userDetails = new User(
                member.getEmployeeId(),
                member.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority(member.getRole().name()))
        );

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);
    }
}
