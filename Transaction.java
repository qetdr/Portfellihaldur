package com.example.beta;

import javafx.scene.Node;

public class Transaction {
    // Assignment
    private int year; // transaction year
    private String transaction; // transaction type: "BUY" or "SELL"
    private String ticker; // ticker of the share
    private double volume; // volume of shares
    private double share_price; // price per share
    private String currency; // currency of the transaction
    private double total_value_EUR; //

    // Constructor
    public Transaction(int year, String transaction, String ticker,
                       double volume, double share_price, String currency, double total_value_EUR) {
        this.year = year;
        this.transaction = transaction;
        this.ticker = ticker;
        this.volume = volume;
        this.share_price = share_price;
        this.currency = currency;
        this.total_value_EUR = total_value_EUR;
    }

    public int getYear() {
        return year;
    }

    public String getTransaction() {
        return transaction;
    }

    public String getTicker() {
        return ticker;
    }

    public double getVolume() {
        return volume;
    }

    public double getShare_price() {
        return share_price;
    }

    public String getCurrency() {
        return currency;
    }

    public double getTotal_value_EUR() {
        return total_value_EUR;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "year=" + year +
                ", transaction='" + transaction + '\'' +
                ", ticker='" + ticker + '\'' +
                ", volume=" + volume +
                ", share_price=" + share_price +
                ", currency='" + currency + '\'' +
                ", total_value_EUR=" + total_value_EUR +
                '}';
    }

    public String failiFormaat() {
        return year+";"+transaction+";"+ticker+";"+volume+";"+share_price+";"+currency;
    }
}
