package com.mongodb.johnlpage.mongoBalanceService.model;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

public class BankBalance {
    @Version
    Long version;

    @Id
    private Integer accountId;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal balance;

    private List<BankTransaction> miniStatement;

    public List<BankTransaction> getMiniStatement() {
        return miniStatement;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

}
