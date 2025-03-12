package Seoul_Milk.sm_server.global.security.provider;

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

public class CustomDaoAuthenticationProvider extends DaoAuthenticationProvider {
    public CustomDaoAuthenticationProvider() {
        // hideUserNotFoundExceptions 값을 false로 설정
        this.setHideUserNotFoundExceptions(false);
    }
}
