package com.mongodb.johnlpage.mongoBalanceService.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.johnlpage.mongoBalanceService.model.BankBalance;
import com.mongodb.johnlpage.mongoBalanceService.model.BankTransaction;
import com.mongodb.johnlpage.mongoBalanceService.repository.BalanceRepository;
import com.mongodb.johnlpage.mongoBalanceService.repository.TransactionRepository;

@RestController

public class BalanceController {

    Logger logger = LoggerFactory.getLogger(BalanceController.class);
    private static Random rng = new Random();

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BalanceRepository balanceRepository;

    @Value("${mongobalance.johnlpage.bootstrapTxnPerAccount}")
    private int bootstrapTxnPerAccount;

    @Value("${mongobalance.johnlpage.nAccounts}")
    int nAccounts;

    /*
     * Fetch Current Balance for an Account
     */
    @GetMapping("/balance/{accountId}")
    public ResponseEntity<BankBalance> GetBalance(@PathVariable long accountId) {
        Optional<BankBalance> balance;

        // FindById is a build in function
        balance = balanceRepository.findById(accountId);
        return ResponseEntity.of(balance);
    }

    /*
     * Fetch a single Transaction
     */

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<BankTransaction> GetTransaction(@PathVariable long transactionId) {
        Optional<BankTransaction> transaction;

        // FindById is a build in function
        transaction = transactionRepository.findById(transactionId);
        return ResponseEntity.of(transaction);
    }

    /*
     * Fetch a Single transaction but specify the account as well as transactionsid
     * If we plan to Shard (Partition) this data then the obvious shard key is
     * accountNo,transacitonId as we
     * Can include that in most queries that are for a specific transactonId
     */

    @GetMapping("/transaction/{accountId}/{transactionId}")
    public ResponseEntity<BankTransaction> GetTransactionWithAccount(@PathVariable long accountId,
            @PathVariable long transactionId) {
        Optional<BankTransaction> transaction;

        transaction = transactionRepository.getTransactionWithAccountId(accountId, transactionId);
        return ResponseEntity.of(transaction);
    }

    @SuppressWarnings("null")
    @GetMapping("/transactions/{accountId}")
    public ResponseEntity<List<BankTransaction>> GetStatement(@PathVariable Long accountId,
            @RequestParam(value = "fromDate", defaultValue = "21000101000000") @DateTimeFormat(pattern = "yyyyMMddHHmmss") Date fromDate,
            @RequestParam(value = "fromId", defaultValue = Long.MAX_VALUE+"" ) Long fromTransaction,
            @RequestParam(value = "n", defaultValue = "10") Integer nTransactions) {
        
        // For Load testing purposes - if accoutnId = -9999 then generate a random one
        if( accountId.equals(-999L)) {
            accountId = BalanceController.rng.nextLong()%BankTransaction.nAccounts  + 1_000_000L;
        }

        List<BankTransaction> transactions = new ArrayList<BankTransaction>();
        transactions = transactionRepository.getNTransactionsAfterDate(accountId, fromDate, fromTransaction,
                nTransactions);
        return new ResponseEntity<List<BankTransaction>>(transactions, HttpStatus.OK);
    }

    /*
     * Post a new Transaction (Repository then updates balance etc.)
     */

    @PostMapping("/transaction")
    public ResponseEntity<String> NewTransaction(@RequestBody BankTransaction newTransaction) {

        // For testing - create one if we didn't push one with an id
        if (newTransaction.getTransactionId() == null) {
            newTransaction = BankTransaction.example();
        }

        transactionRepository.recordTransaction(newTransaction);
        return new ResponseEntity<String>(HttpStatus.CREATED);
    }

    @PostMapping("/v2/transaction")
    public ResponseEntity<String> NewTransactionV2(@RequestBody BankTransaction newTransaction) {

        // For testing - create one if we didn't push one with an id
        if (newTransaction.getTransactionId() == null) {
            newTransaction = BankTransaction.example();
        }

        transactionRepository.recordTransaction_V2(newTransaction);
        return new ResponseEntity<String>(HttpStatus.CREATED);
    }

    // In this - the details of the last 10 transactions are stored in the account balance
    // Making them faster to retrieve

    @PostMapping("/v3/transaction")
    public ResponseEntity<String> NewTransactionV3(@RequestBody BankTransaction newTransaction) {

        // For testing - create one if we didn't push one with an id
        if (newTransaction.getTransactionId() == null) {
            newTransaction = BankTransaction.example();
        }

        transactionRepository.recordTransaction_V3(newTransaction);
        return new ResponseEntity<String>(HttpStatus.CREATED);
    }


    /*
     * load a lot of transactions, this just cuts down on making many webservice
     * calls
     * It could be optimized by turning these into bulk operations and committing
     * say 1000
     * at the same time but this is simpler.
     */

    @PostMapping("/bootstrap")
    public ResponseEntity<String> BootstrapAccounts() {

        int bootstrapTxn = bootstrapTxnPerAccount * nAccounts;
        logger.info("Thread bulk loading " + bootstrapTxn + " transactions");
        for (int x = 0; x < bootstrapTxn; x++) {
            BankTransaction newTransaction = BankTransaction.example();
            try {
                transactionRepository.recordTransaction(newTransaction);
            } catch (Exception e) {
                System.err.println("Error updating MongoDB");
                System.err.println(e.getMessage()); // Might get dups here
            }
        }

        return new ResponseEntity<String>(HttpStatus.CREATED);
    }
}
