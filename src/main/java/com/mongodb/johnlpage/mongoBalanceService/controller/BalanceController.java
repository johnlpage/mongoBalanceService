package com.mongodb.johnlpage.mongoBalanceService.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.mongodb.johnlpage.mongoBalanceService.model.BankTransaction;
import com.mongodb.johnlpage.mongoBalanceService.repository.TransactionRepository;

@RestController

public class BalanceController {
      @Autowired
    private TransactionRepository transactionRepository;

    @SuppressWarnings("null")
    @PostMapping("/transaction")
    public ResponseEntity<String> NewTransaction(@RequestBody BankTransaction newTransaction) {

        // For testing - create one if we didn't push one with an id
        if (newTransaction.getTransactionId() == null) {
            newTransaction = BankTransaction.example();
        }

        transactionRepository.recordTransaction(newTransaction);
        return new ResponseEntity<String>(HttpStatus.CREATED);
    }

    //Load in at least one transaction per account
    @SuppressWarnings("null")
    @PostMapping("/bootstrap")
    public ResponseEntity<String> BootstrapAccounts() {
        
        for(int accountNo=0;accountNo<BankTransaction.nAccounts;accountNo++) {
            BankTransaction newTransaction = BankTransaction.example();
            try {
                transactionRepository.recordTransaction(newTransaction);
            } catch( Exception e) {
                System.err.println("Error updating MongoDB");
                System.err.println(e.getMessage()); //Might get dups here
            }
        }
       
    
        return new ResponseEntity<String>(HttpStatus.CREATED);
    }

}

