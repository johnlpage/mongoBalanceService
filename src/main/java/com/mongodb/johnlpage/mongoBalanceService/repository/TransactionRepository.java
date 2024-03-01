package com.mongodb.johnlpage.mongoBalanceService.repository;

import org.springframework.data.mongodb.repository.MongoRepository;


import com.mongodb.johnlpage.mongoBalanceService.model.BankTransaction;

public interface TransactionRepository extends MongoRepository<BankTransaction, Long>, CustomTransactionRepository { 
}