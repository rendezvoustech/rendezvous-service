package tech.rendezvous.rendezvousservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class RendezvousServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RendezvousServiceApplication.class, args);
	}

}
