package com.example.beta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileToList {

    private String file_name;

    public FileToList(String file_name) {
        this.file_name = file_name;
    }

    public List<NewPrices> readPrices() throws Exception {
        // Import the file with transactions
        File fail = new File(file_name);
        List<NewPrices> new_prices = new ArrayList<>();
        try (Scanner sc = new Scanner(fail, "UTF-8")) {
            // Start reading lines
            sc.nextLine(); // Ignore the first line, since it contains column names
            while (sc.hasNextLine()) {
                String l1 = sc.nextLine();
                String[] l1_parts = l1.split(";");
                // since new prices file has 3 elements, and transaction has more, include logic
                // for new prices
                String ticker = l1_parts[0];
                double new_price = Double.parseDouble(l1_parts[1]);
                String currency = l1_parts[2];
                new_prices.add(new NewPrices(ticker, new_price, currency));

            } // while kinni
        }  return new_prices;// return new_prices

    }// try kinni

    // transactions
    public List<Transaction> readTransactions() throws Exception {
        // Import the file with transactions
        File fail = new File(file_name);
        List<Transaction> transactions = new ArrayList<>();
        try (Scanner sc = new Scanner(fail, "UTF-8")) {
            // Start reading lines
            sc.nextLine(); // Ignore the first line, since it contains column names
            while (sc.hasNextLine()) {
                String l1 = sc.nextLine();
                String[] l1_parts = l1.split(";");
                // transactions
                int year = Integer.parseInt(l1_parts[0]);
                String transaction = l1_parts[1];
                String ticker = l1_parts[2];
                double volume = Double.parseDouble(l1_parts[3]);
                double share_price = Double.parseDouble(l1_parts[4]);
                String currency = l1_parts[5];
                double total_value = volume * share_price;
                double total_value_EUR = total_value;

                if (currency.equals("USD")) {
                    total_value_EUR = total_value * PortfolioManager_beta.getUSD_to_EUR();
                }
                transactions.add(new Transaction(year, transaction, ticker, volume, share_price, currency, total_value_EUR));
            } // while kinni
        }  return transactions; // return transactions
    }// try kinni





}
