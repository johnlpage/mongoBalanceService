package com.mongodb.johnlpage.mongoBalanceService.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mongodb.johnlpage.mongoBalanceService.model.BankBalance;

public interface BalanceRepository extends MongoRepository<BankBalance, Long> { 

    
}