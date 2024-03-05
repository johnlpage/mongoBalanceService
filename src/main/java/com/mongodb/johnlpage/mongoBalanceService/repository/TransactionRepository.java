package com.mongodb.johnlpage.mongoBalanceService.repository;



import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.mongodb.johnlpage.mongoBalanceService.model.BankTransaction;

public interface TransactionRepository extends MongoRepository<BankTransaction, Long>, CustomTransactionRepository { 

     @Query(value = "{'accountId': ?0 , 'transactionId': ?1 }")
    Optional<BankTransaction> getTransactionWithAccountId(Long accountId , Long transactionId);

}