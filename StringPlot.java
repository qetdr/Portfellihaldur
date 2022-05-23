package com.example.beta;

public class StringPlot {

    private String ticker;
    private double proportion;
    private double ticker_value_eur;

    public StringPlot(String ticker, double proportion, double ticker_value_eur) {
        this.ticker = ticker;
        this.proportion = proportion;
        this.ticker_value_eur = ticker_value_eur;
    }

    private int bar_length; // how long is each plot bar (i.e., how many hastags max?

    public String returnStringPlot(int bar_length){
        int n_hashtags = (int) ((proportion)*(bar_length)); // Compute the number of hashtags (bar plot)
        String ticker_sout; // for better alignment, add some spaces to ticker (if less than 7 characters)
        if (ticker.length() < 7){
            ticker_sout = ticker + " ".repeat(7 - ticker.length());
        } else{
            ticker_sout = ticker;
        }
        // The "plot"
        return "|" + "#".repeat(n_hashtags) + "-".repeat(bar_length-n_hashtags) + "|" + " "
                + ticker_sout + proportion*100 + "% " + "(" + ticker_value_eur + " EUR)";
    }
}