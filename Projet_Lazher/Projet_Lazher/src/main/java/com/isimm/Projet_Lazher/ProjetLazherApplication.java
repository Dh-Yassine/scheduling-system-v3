package com.isimm.Projet_Lazher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.isimm.Projet_Lazher.model")
@EnableJpaRepositories("com.isimm.Projet_Lazher.repository")
public class ProjetLazherApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjetLazherApplication.class, args);
	}

}
