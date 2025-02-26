package Seoul_Milk.sm_server.global.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

@Configuration
public class MailConfig {

    /**
     * Thymeleaf 파일 설정을 위한 빈
     */
    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setPrefix("classpath:/templates/"); // 템플릿 경로
        resolver.setSuffix(".html");                 // 템플릿 파일 확장자
        resolver.setTemplateMode(TemplateMode.HTML); // 템플릿 모드
        resolver.setCharacterEncoding("UTF-8");      // UTF-8 설정
        return resolver;
    }

    /**
     * Thymeleaf 템플릿 엔진
     */
    @Bean
    public SpringTemplateEngine templateEngine(@Qualifier("templateResolver") SpringResourceTemplateResolver templateResolver) {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templateResolver);
        return engine;
    }
}
