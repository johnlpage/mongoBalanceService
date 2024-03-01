package com.mongodb.johnlpage.mongoBalanceService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class MongoBalanceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MongoBalanceServiceApplication.class, args);
	}

}
