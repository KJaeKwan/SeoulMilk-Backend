package Seoul_Milk.sm_server.global.config;

import Seoul_Milk.sm_server.global.exception.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    // 인증이 필요하지 않은 URL 목록
    private final String[] allowedUrls = {
            "/",
            "/swagger-ui/**",
            "/v3/api-docs/**",
    };


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // CORS 설정
        http.cors(Customizer.withDefaults());

        // 경로별 인가 설정
        http.authorizeHttpRequests((auth) -> auth
                .requestMatchers(allowedUrls).permitAll()
                .anyRequest().authenticated());

        // 예외 처리 설정
        http.exceptionHandling(e -> e
                .authenticationEntryPoint(customAuthenticationEntryPoint));

        return http.build();
    }

}
