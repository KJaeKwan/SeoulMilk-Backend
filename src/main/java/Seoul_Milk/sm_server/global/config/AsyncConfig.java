package Seoul_Milk.sm_server.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "ocrTaskExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);  // 최소 10개 스레드
        executor.setMaxPoolSize(50);   // 최대 50개 스레드
        executor.setQueueCapacity(100); // 대기열 설정
        executor.setThreadNamePrefix("OCR-Async-");
        executor.initialize();
        return executor;
    }
}
