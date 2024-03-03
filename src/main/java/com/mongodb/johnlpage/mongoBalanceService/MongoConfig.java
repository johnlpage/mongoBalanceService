package com.mongodb.johnlpage.mongoBalanceService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.retry.annotation.EnableRetry;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
@EnableRetry
@EnableMongoRepositories(basePackages = "com.mongodb.johnlpage.mongoBalanceService")
public class MongoConfig extends AbstractMongoClientConfiguration{


    @Value("${spring.data.mongodb.uri}")
    private String uri;
    
    @Value("${mongobalance.johnlpage.nAccounts}")
    private String db;
   
    // Auto Index Creation is OK for development/testing but not best in production
    @Override
    public boolean autoIndexCreation() {
      return true;
    }

    @Bean
    MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }

    @Override
    protected String getDatabaseName() {
        return db;
    }
    @Bean
    @Override
    public MongoClient mongoClient() {
        final ConnectionString connectionString = new ConnectionString(uri);
        final MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .build();
        return MongoClients.create(mongoClientSettings);
    }
}