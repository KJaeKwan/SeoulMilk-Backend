package Seoul_Milk.sm_server.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("Seoul Milk Project API Docs")
                .description("\uD83E\uDD5B[큐시즘] X [서울 우유] 기업 프로젝트 \"돈까스 클럽\"팀 API 명세서 입니다. \uD83E\uDD5B");
    }
}
