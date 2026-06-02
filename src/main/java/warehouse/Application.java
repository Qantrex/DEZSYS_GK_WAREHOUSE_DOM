package warehouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

/**
 * Central document oriented middleware (Spring Boot + Spring Data MongoDB).
 *
 * The application boots an embedded web server that exposes the REST interface
 * ({@link warehouse.controller.WarehouseController}) and persists all incoming
 * data in MongoDB. {@code @EnableScheduling} activates the continuous storage
 * scheduler ({@link warehouse.scheduler.WarehouseScheduler}).
 */
@SpringBootApplication
@EnableScheduling
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	/** Used by the data generator client to talk to the own REST interface. */
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
