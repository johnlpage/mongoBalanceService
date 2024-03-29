package com.mongodb.johnlpage.mongoBalanceService.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.SessionSynchronization;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.Update.PushOperatorBuilder;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Transactional;

import com.mongodb.johnlpage.mongoBalanceService.model.BalanceHistory;
import com.mongodb.johnlpage.mongoBalanceService.model.BankBalance;
import com.mongodb.johnlpage.mongoBalanceService.model.BankTransaction;

public class CustomTransactionRepositoryImpl implements CustomTransactionRepository {

    @Autowired
    private MongoTemplate template;
    @Autowired
    private BalanceRepository balanceRepository;

    // This is the code where we define our Transaction
    @Transactional
    @Retryable
    public boolean recordTransaction(BankTransaction newTransaction) {
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
        bh.setTransactionId(newTransaction.getTransactionId());
        bh.setBalance(postUpdate.getBalance());
        bh.setChange(newTransaction.getAmmount());
        template.insert(bh);

        return true;
    }


    @Transactional
    @Retryable
    public boolean recordTransaction_V2(BankTransaction newTransaction) {
        // Here we record the Transaction and the resulting Balance Update together

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
        bh.setTransactionId(newTransaction.getTransactionId());
        bh.setBalance(postUpdate.getBalance());
        bh.setChange(newTransaction.getAmmount());
        newTransaction.setBalanceHistory(bh);
        template.insert(newTransaction);

        return true;
    }

    @Transactional
    @Retryable
    public boolean recordTransaction_V3(BankTransaction newTransaction) {
        // Here we record the Transaction and the resulting Balance Update together

        // Update the balance
        // Use Spring not Mongo Syntax here
        // As well as changing the balance - we are also adding this transaction to 
        // An array if it's newer then any in there - the array is sorted and capped at 10 elements
        // This is a cache of the latest 10 transactions to speed up that specific read.

        Query query = new Query(Criteria.where("accountId").is(newTransaction.getAccountId()));
        Update updateBalance = new Update();
        updateBalance.inc("balance", newTransaction.getAmmount());
        PushOperatorBuilder pushUpdate = updateBalance.push("miniStatement");
        updateBalance = pushUpdate.sort(Sort.by(Direction.DESC,"transactionDate")).slice(10).each(newTransaction);

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
        bh.setTransactionId(newTransaction.getTransactionId());
        bh.setBalance(postUpdate.getBalance());
        bh.setChange(newTransaction.getAmmount());
        newTransaction.setBalanceHistory(bh);
        template.insert(newTransaction);

        return true;
    }

    public List<BankTransaction> getNTransactionsAfterDateWithCache(long accountId, Date fromDate, long fromTransaction, int nTransactions) {
        // If fromDate is in future and nTransactions < 10 pull the cached verison

        if(nTransactions > 10 || fromDate.before(new Date())) {
            return getNTransactionsAfterDate( accountId,  fromDate,  fromTransaction,  nTransactions) ;
        }
        Optional<BankBalance> b =  balanceRepository.findById(accountId);
        if(b.isEmpty()) return new ArrayList<BankTransaction>();
        return b.get().getMiniStatement();
    }

    public List<BankTransaction> getNTransactionsAfterDate(long accountId, Date fromDate, long fromTransaction, int nTransactions) {
       /*
        * We could do this simply by adding an annotations in TransactionRepository
        * Or even a correctly names function in the Repository
        * But by pulling it out to explicit  MongoTemplate we can do more optimal things
        *
        * We include fromId in here - which could be null to differentiate between transactions with the same
        * timestamp when paging - we aren't using the Spring Data  native paging because we have an optimization to add
        * for the first page (most common request)
        */

        //Fetch from most recent first
        
        Query query = new Query(Criteria.where("accountId").is(accountId)
        .and("transactionDate").lte(fromDate)
        .and("transactionId").lt(fromTransaction));

        // Order by account,date,id so we have a stable paging model
        ArrayList<Order> sortOrder = new ArrayList<>();
        sortOrder.add(new Sort.Order(Direction.DESC,"accountId"));
        sortOrder.add(new Sort.Order(Direction.DESC,"transactionDate"));
        sortOrder.add(new Sort.Order(Direction.DESC,"transactionId"));
        query.with(Sort.by(sortOrder));


        final Pageable firstN = PageRequest.of(0, nTransactions);
        query.with(firstN);

        List<BankTransaction>  transactions = template.find(query,BankTransaction.class);
        return transactions;
    }


}