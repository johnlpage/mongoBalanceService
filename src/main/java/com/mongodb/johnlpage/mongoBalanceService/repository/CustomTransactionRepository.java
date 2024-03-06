package com.mongodb.johnlpage.mongoBalanceService.repository;

import java.util.Date;
import java.util.List;

import com.mongodb.johnlpage.mongoBalanceService.model.BankTransaction;

interface CustomTransactionRepository {
    boolean recordTransaction(BankTransaction newTransaction) ;
    boolean recordTransaction_V2(BankTransaction newTransaction) ;
    boolean recordTransaction_V3(BankTransaction newTransaction) ;

    List<BankTransaction> getNTransactionsAfterDate( long accountId, Date fromDate,long fromTransaction, int nTransactions );
    List<BankTransaction> getNTransactionsAfterDateWithCache( long accountId, Date fromDate,long fromTransaction, int nTransactions );
}
