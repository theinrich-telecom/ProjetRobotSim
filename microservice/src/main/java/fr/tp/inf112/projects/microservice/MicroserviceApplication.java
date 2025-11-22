package fr.tp.inf112.projects.microservice;

import fr.tp.inf112.projects.robotsim.model.Factory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroserviceApplication.class, args);

		System.out.println("test");
		Factory factory;
	}

}
