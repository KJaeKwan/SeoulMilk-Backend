package Seoul_Milk.sm_server.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("http://localhost:5173");
        config.addAllowedOriginPattern("https://sm-frontend-eosin.vercel.app");
        config.addAllowedOriginPattern("http://localhost:8080");
        config.addAllowedOriginPattern("https://jk-project.site");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.addExposedHeader("access");
        config.addAllowedHeader("Authorization");

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}