package com.mongodb.johnlpage.mongoBalanceService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
@EnableMongoRepositories(basePackages = "com.mongodb.johnlpage.mongoBalanceService")
public class MongoConfig extends AbstractMongoClientConfiguration{


    @Value("${spring.data.mongodb.uri}")
    private String uri;
    
    @Value("${spring.data.mongodb.database}")
    private String db;
   
    @Bean
    MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        System.out.println("HERE HERE HERE <=====================");
        return new MongoTransactionManager(dbFactory);
    }

    @Override
    protected String getDatabaseName() {
        return db;
    }
    @Bean
    @Override
    public MongoClient mongoClient() {
        final ConnectionString connectionString = new ConnectionString("mongodb://localhost:27017/bankbalance?replicaSet=repset");
        final MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .build();
        return MongoClients.create(mongoClientSettings);
    }
}