package com.bigobooks.purchases_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
		"com.bigobooks.purchases_service",
		"com.bigobooks.util"
})
@EntityScan(basePackages = { "com.bigobooks.entities" })
@EnableJpaRepositories(basePackages = { "com.bigobooks.purchases_service.repository" })
@EnableFeignClients
@EnableCaching
public class PurchasesServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PurchasesServiceApplication.class, args);
	}

}
