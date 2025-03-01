package Seoul_Milk.sm_server.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.List;

@Configuration
@EnableWebMvc
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("https://jk-project.site").description("Production Server"),
                        new Server().url("http://localhost:8080").description("Local Server")
                ))
                .components(components())
                .info(apiInfo())
                .addSecurityItem(securityRequirement());
    }

    private Info apiInfo() {
        return new Info()
                .title("Seoul Milk Project API Docs")
                .description("\uD83E\uDD5B[큐시즘] X [서울 우유] 기업 프로젝트 \"돈까스 클럽\"팀 API 명세서 입니다. \uD83E\uDD5B");
    }

    // JWT 토큰을 위한 보안 컴포넌트 설정
    private Components components() {
        String securityScheme = "JWT TOKEN";
        return new Components()
                .addSecuritySchemes(securityScheme, new SecurityScheme()
                        .name(securityScheme)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("Bearer")
                        .bearerFormat("JWT"));
    }

    // 보안 요구사항 설정
    private SecurityRequirement securityRequirement() {
        String securityScheme = "JWT TOKEN";
        return new SecurityRequirement().addList(securityScheme);
    }
}
