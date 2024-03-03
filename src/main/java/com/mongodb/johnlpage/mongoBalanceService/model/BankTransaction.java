package com.mongodb.johnlpage.mongoBalanceService.model;

import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

//We can fetch all the transactions and join the balance if we need to

@CompoundIndexes({
        @CompoundIndex(name = "account_date", def = "{'accountId' : 1, 'transactionDate': 1}")
})

@Document("transactions")
@Component
public class BankTransaction {


    public static int nAccounts;

    @Value("${mongobalance.johnlpage.nAccounts}")
    public void setNameStatic(int name){
        BankTransaction.nAccounts = name;
    }

    static enum TransactionType {
        WITHDRAWL, DEPOSIT
    }
    @Version Long version;
    private static Random RNG = null;
    @Id
    private Long transactionId;
    private Integer customerId;
    private Integer accountId;
    private String reference;
    // Have to explicitly call out Decimal128 otherwise string :-(
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal ammount;
 
    private Date transactionDate;
    private String IBAN;
    private TransactionType transactionType;
    private List<String> flags;
    private HashMap<String,Object> requestJSON;

    BankTransaction() {
       

        this.flags = new ArrayList<String>();
    }

    public List<String> getFlags() {
        return flags;
    }

    public void setFlags(List<String> flags) {
        this.flags = flags;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public void setAmmount(BigDecimal ammount) {
        this.ammount = ammount;
    }

    public void setIBAN(String IBAN) {
        this.IBAN = IBAN;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getIBAN() {
        return IBAN;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public String getReference() {
        return reference;
    }

    public BigDecimal getAmmount() {
        return ammount;
    }

    // Generate a fake transaction for testing
    public static BankTransaction example() {
       
        if (RNG == null) {
            RNG = new Random();
        }
        BankTransaction t = new BankTransaction();
        //We will get some collisions here - we should gracefully ignore repeats.

        t.transactionId = RNG.nextLong(Long.MAX_VALUE - 100_000_000) + 100_000_000;
   
        t.accountId = RNG.nextInt(BankTransaction.nAccounts) + 1_000_000; 

        t.customerId = (t.accountId *3 ) + RNG.nextInt(3); // 3 Customers per account 
     
        t.reference = ""; // TODO generate some value
        // more positive than negative values to accounts go up.
        t.ammount = new BigDecimal(Double.toString(RNG.nextDouble(1000) - 400.0)).setScale(2, RoundingMode.HALF_UP);
   
        if (t.ammount.compareTo(BigDecimal.ZERO) > 0) {
            t.transactionType = TransactionType.DEPOSIT;
        } else {
            t.transactionType = TransactionType.WITHDRAWL;
        }
        t.IBAN = "XX" + RNG.nextLong(1_000_000_000) + RNG.nextLong(1_000_000_000);
        long unixTime = new Date().getTime() - RNG.nextLong(400_000_000_000L);
        t.transactionDate = new Date(unixTime);

	  
        // Just making arbitrary Map here
        // t.requestJSON = new HashMap<String, Object>(org.bson.Document.parse(" { \"a\" : 1, \"b\":{ \"c\": 2 } } "));
        return t;
    }



}
