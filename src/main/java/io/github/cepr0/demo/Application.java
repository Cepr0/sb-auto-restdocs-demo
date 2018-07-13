package io.github.cepr0.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.hateoas.config.EnableHypermediaSupport;

import java.util.stream.IntStream;

import static org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType.HAL;

@RequiredArgsConstructor
@SpringBootApplication
@EnableHypermediaSupport(type = HAL)
public class Application {
	
	private final UserRepo userRepo;
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	@EventListener
	public void onReady(ApplicationReadyEvent e) {
		// Populate demo data
		IntStream.range(0, 3).mapToObj(i -> new User("User" + i, i + 10)).forEach(userRepo::create);
	}
}
