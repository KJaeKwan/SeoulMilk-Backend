package Seoul_Milk.sm_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class SM_serverApplication {

	public static void main(String[] args) {
		SpringApplication.run(SM_serverApplication.class, args);
	}

}
