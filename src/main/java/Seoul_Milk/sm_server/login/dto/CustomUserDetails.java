package Seoul_Milk.sm_server.login.dto;

import Seoul_Milk.sm_server.login.entity.MemberEntity;
import java.util.ArrayList;
import java.util.Collection;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@AllArgsConstructor
public class CustomUserDetails {
    private final MemberEntity memberEntity;

    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return memberEntity.getRole().toString();
            }
        });
        return collection;
    }

    public String getPassword() {
        return memberEntity.getPassword();
    }

    public String getEmployeeId() {
        return memberEntity.getEmployeeId();
    }

    boolean isAccountNonExpired() {
        return true;
    }

    boolean isAccountNonLocked() {
        return true;
    }

    boolean isCredentialsNonExpired() {
        return true;
    }

    boolean isEnabled() {
        return true;
    }
}
