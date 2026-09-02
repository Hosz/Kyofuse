package com.hokyozu.kyofuse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class KyofuseApplication {

	public static void main(String[] args) {
		SpringApplication.run(KyofuseApplication.class, args);
	}

}
