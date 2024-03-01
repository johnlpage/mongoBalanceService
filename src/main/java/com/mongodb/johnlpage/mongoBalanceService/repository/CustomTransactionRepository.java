package com.mongodb.johnlpage.mongoBalanceService.repository;

import com.mongodb.johnlpage.mongoBalanceService.model.BankTransaction;

interface CustomTransactionRepository {
    long recordTransaction(BankTransaction newTransaction) ;
}
