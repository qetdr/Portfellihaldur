package com.example.beta;

public class NewPrices {
    private String ticker;
    private double new_price;
    private String currency;

    public NewPrices(String ticker, double new_price, String currency) {
        this.ticker = ticker;
        this.new_price = new_price;
        this.currency = currency;
    }

    public String getTicker() {
        return ticker;
    }

    public double getNew_price() {
        return new_price;
    }

    public String getCurrency() {
        return currency;
    }

    public double getNewEurPrice() {
        double price = new_price;
        if (currency.equals("USD")) {
            price = price * PortfolioManager_beta.getUSD_to_EUR();
        }
        return price;
    }

    @Override
    public String toString() {
        return "NewPrices{" +
                "ticker='" + ticker + '\'' +
                ", new_price=" + new_price +
                ", currency='" + currency + '\'' +
                '}';
    }

    public String failiFormaat() {
        return ticker+";"+new_price+";"+currency;
    }
}