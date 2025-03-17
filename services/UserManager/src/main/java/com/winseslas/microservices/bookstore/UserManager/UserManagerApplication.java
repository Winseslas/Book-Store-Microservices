package com.winseslas.microservices.bookstore.UserManager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@EnableDiscoveryClient
@EnableJpaRepositories(basePackages = "com.winseslas.microservices.bookstore.UserManager.repository")
@EntityScan(basePackages = "com.winseslas.microservices.bookstore.UserManager.model")
public class UserManagerApplication {
	public static void main(String[] args) {
		SpringApplication.run(UserManagerApplication.class, args);
	}
}
