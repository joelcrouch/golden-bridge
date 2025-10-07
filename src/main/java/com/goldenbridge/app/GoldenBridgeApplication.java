package com.goldenbridge.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class GoldenBridgeApplication {

	public static void main(String[] args) {
		SpringApplication.run(GoldenBridgeApplication.class, args);
	}

}
