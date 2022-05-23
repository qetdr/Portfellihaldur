package com.example.beta;

public class OsakuteOsakaal implements Comparable<OsakuteOsakaal>{
    private String osakuNimi;
    private double praeguneEurHind;
    private double proportsioon;

    public OsakuteOsakaal(String osakuNimi, double praeguneEurHind, double proportsioon) {
        this.osakuNimi = osakuNimi;
        this.praeguneEurHind = praeguneEurHind;
        this.proportsioon = proportsioon;
    }

    public String getOsakuNimi() {
        return osakuNimi;
    }

    public double getPraeguneEurHind() {
        return praeguneEurHind;
    }

    public double getProportsioon() {
        return proportsioon;
    }

    public void lisaHinda(double mingihind) {
        praeguneEurHind += mingihind;
    }

    public void lisaProportsiooni (double mingiproportsioon) {
        proportsioon += mingiproportsioon;
    }

    @Override
    public int compareTo(OsakuteOsakaal o) {
        if(proportsioon>o.proportsioon) return -1;
        else if (proportsioon<o.proportsioon) return 1;
        return 0;
    }

    @Override
    public String toString() {
        return "OsakuteOsakaal{" +
                "osakuNimi='" + osakuNimi + '\'' +
                ", praeguneEurHind=" + praeguneEurHind +
                ", proportsioon=" + proportsioon +
                '}';
    }
}
