package com.mongodb.johnlpage.mongoBalanceService.repository;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.SessionSynchronization;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Transactional;

import com.mongodb.client.MongoClient;
import com.mongodb.johnlpage.mongoBalanceService.model.BalanceHistory;
import com.mongodb.johnlpage.mongoBalanceService.model.BankBalance;
import com.mongodb.johnlpage.mongoBalanceService.model.BankTransaction;


public class CustomTransactionRepositoryImpl implements CustomTransactionRepository {
   
    @Autowired
    private MongoTemplate template;
    // This is the code where we define our Transaction
    @Transactional
    @Retryable 
    public long recordTransaction(BankTransaction newTransaction) {
        // MongoTemplate is like a mongoClient with Object mapping
     

        template.insert(newTransaction);

        // Update the balance
        // Use Spring not Mongo Syntax here
        Query query = new Query(Criteria.where("accountId").is(newTransaction.getAccountId()));
        Update updateBalance = new Update();
        updateBalance.inc("balance", newTransaction.getAmmount());

        // Using findAndModify Because we want to see what it looks like after the
        // change.
        // We dont want to read it up front.
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.upsert(true);
        options.returnNew(true);

        BankBalance postUpdate = template.findAndModify(query, updateBalance, options, BankBalance.class);

        // Record the balanceHistory
        BalanceHistory bh = new BalanceHistory();
        bh.setAccountId(newTransaction.getAccountId());
        bh.setUpdateTime(new Date());
        bh.setBalance(postUpdate.getBalance());
        bh.setChange(newTransaction.getAmmount());
        template.insert(bh);

        return 0;
    }
}