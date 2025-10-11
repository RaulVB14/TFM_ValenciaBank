package com.valenciaBank.valenciaBank.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TransactionData {

    @JsonProperty("transaction")
    private Transaction transaction;

    @JsonProperty("user")
    private String user;

    public TransactionData() {
    }

    public TransactionData(Transaction transaction, String user) {
        this.transaction = transaction;
        this.user = user;
    }

    // Getters y setters
    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
