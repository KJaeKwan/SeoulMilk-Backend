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
        executor.setCorePoolSize(10);  // 최소 스레드
        executor.setMaxPoolSize(20);   // 최대 스레드
        executor.setQueueCapacity(50); // 대기열 설정
        executor.setKeepAliveSeconds(10);
        executor.setThreadNamePrefix("OCR-Async-");

        executor.initialize();
        return executor;
    }

    @Bean(name = "mailTaskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("Mail-Executor-");
        executor.initialize();
        return executor;
    }
}
