package com.example.beta;

import com.example.beta.FileToList;
import com.example.beta.NewPrices;
import com.example.beta.Transaction;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.util.*;

public class PortfolioManager_beta extends Application {
    // ---------------- ABIMEETODID ----------------------
    // Peidame kõik sõnumid mingis listis (meil errorid ja õnnestumise teadaanded)
    public void peidaSõnumid(List<Text> peidetavadSõnumid) {
        for (Text text : peidetavadSõnumid) {
            text.setManaged(false);
            text.setVisible(false);
        }
    }

    public void peidaSõnum(Text tekst) {
        tekst.setManaged(false);
        tekst.setVisible(false);
    }

    // Kuva teated N sekundit
    public void kuva2sek(Text tekstiblokk) {
        PauseTransition pt = new PauseTransition(Duration.seconds(2));
        pt.setOnFinished(event -> {
            tekstiblokk.setManaged(false);
            tekstiblokk.setVisible(false);
        });
        tekstiblokk.setManaged(true);
        tekstiblokk.setVisible(true);
        pt.play();
    }

    // Lisame mingisse tehingutelisti ühe tehingu juurde
    public void lisaTehing(List<Transaction> tehinguteList, String rida) throws Exception{
        try {
            String[] reaosad = rida.split(";");
            Double totalEurValue = Double.parseDouble(reaosad[3]) * Double.parseDouble(reaosad[4]);
            if (reaosad[5].equals("USD")) totalEurValue = totalEurValue * getUSD_to_EUR();
            Transaction uusTehing = new Transaction(Integer.parseInt(reaosad[0]), reaosad[1], reaosad[2],
                    Double.parseDouble(reaosad[3]), Double.parseDouble(reaosad[4]), reaosad[5], totalEurValue);
            tehinguteList.add(uusTehing);
        } catch (Exception e) {
            throw new Exception();
        }
    }

    // Lisame mingisse osakutehindadelisti ühe osakuhinna juurde
    public void lisaHind(Map<String,Double> hinnaMap, String rida) throws Exception {
        try {
            String[] reaosad = rida.split(";");
            NewPrices uusOsakuhind = new NewPrices(reaosad[0],Double.parseDouble(reaosad[1]),reaosad[2]);
            hinnaMap.put(uusOsakuhind.getTicker(), uusOsakuhind.getNewEurPrice());

        } catch (Exception e) {
            throw new Exception();
        }
    }

    // Tõstame listist hinnad üle hoopis mappi
    public void lisaHinnadMapi(List<NewPrices> hinnaList, Map<String,Double> hinnaMap) {
        for (NewPrices newPrices : hinnaList) {
            hinnaMap.put(newPrices.getTicker(), newPrices.getNewEurPrice());
        }
    }

    // Salvestame kõik tehingud meie tehingutelistist mingisse tahetud faili õiges formaadis
    public void salvestaTehingudFaili(List<Transaction> tehinguteList, String failinimi) throws Exception {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(failinimi), "UTF-8")) {
            writer.write("YEAR;TRANS_TYPE;TICKER;VOL;SHARE_PRICE;CURRENCY\n");
            for (int i = 0; i < tehinguteList.size(); i++) {
                if (i == tehinguteList.size()-1) writer.write(tehinguteList.get(i).failiFormaat());
                else writer.write(tehinguteList.get(i).failiFormaat()+"\n");
            }

        } catch (Exception e) {
            throw new Exception();
        }
    }

    // Salvestame kõik hinnad meie osakuteHindadeListist mingisse tahetud faili õiges formaadis
    private void salvestaHinnadFaili(Map<String,Double> osakuteHinnadMap, String failinimi) throws Exception {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(failinimi), "UTF-8")) {
            writer.write("TICKER;PRICE;CURRENCY\n");
            var ref = new Object() {
                int i = osakuteHinnadMap.size();
            };
            osakuteHinnadMap.forEach( (k,v) -> {
                try {
                    if (ref.i==1) writer.write(k+";"+v+";EUR");
                    else writer.write(k+";"+v+";EUR\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    ref.i -= 1;
                }
            });
        } catch (Exception e) {
            throw new Exception();
        }
    }

    // Meetodid alfa-versioonist
    // Calculate total buy volume
    public static double totalAmountBought(List<Transaction> transactionSheet) {
        double totalInvested = 0;
        for (Transaction transaction1 : transactionSheet) {
            double volumeOnTransaction = transaction1.getTotal_value_EUR();
            if(transaction1.getTransaction().equals("BUY")) totalInvested+= volumeOnTransaction;
        }
        return totalInvested;
    }

    // Calculate total sell volume
    public static double totalAmountSold(List<Transaction> transactionSheet) {
        double totalInvested = 0;
        for (Transaction transaction1 : transactionSheet) {
            double volumeOnTransaction = transaction1.getTotal_value_EUR();
            if(transaction1.getTransaction().equals("SELL")) totalInvested+= volumeOnTransaction;
        }
        return totalInvested;
    }

    // Find unique tickers
    public static List<String> findUniqueTickers(List<Transaction> transactions) {
        List<String> unique_tickers= new ArrayList<>();
        for (Transaction transaction : transactions) {
            String ticker = transaction.getTicker();
            if (!unique_tickers.contains(ticker)){ // if ticker not in unique ticker list, add it + add the value
                unique_tickers.add(ticker);
            }
        }
        return unique_tickers;
    }

    // Find total size of holding of each ticker
    public static List<Double> findTotalVolume(List<Transaction> transaction) {
        List<Double> total_volume = new ArrayList<>();
        List<String> unique_tickers = new ArrayList<>();
        for (Transaction transaction1 : transaction) {
            String ticker = transaction1.getTicker();
            double totalVolume = transaction1.getVolume();
            // if ticker not in unique ticker list, add it + add the value
            if (!unique_tickers.contains(ticker)){
                unique_tickers.add(ticker);
                total_volume.add(totalVolume);
            } else{
                // find the ticker index if in list, add the value to previous value
                int ticker_index = unique_tickers.indexOf(ticker);
                if (transaction1.getTransaction().equals("BUY")){
                    total_volume.set(ticker_index,(total_volume.get(ticker_index) + totalVolume));
                } else{
                    total_volume.set(ticker_index,(total_volume.get(ticker_index) - totalVolume));
                }
            }
        }
        return total_volume;
    }

    // Find unique tickers from the current prices file
    public static List<String> findUniqueTickerPrices(List<NewPrices> new_prices) {
        List<String> unique_tickers_prices = new ArrayList<>();
        for (NewPrices new_price : new_prices) {
            String ticker = new_price.getTicker();
            if(!unique_tickers_prices.contains(ticker)) unique_tickers_prices.add(ticker);
        }
        return unique_tickers_prices;
    }

    // USD to EUR conversion rate & method to get it
    public static double USD_to_EUR = 0.958059;
    public static double getUSD_to_EUR() {
        return USD_to_EUR;
    }

    // Find the current price of each ticker from the prices file
    public static List<Double> findPrices (List<NewPrices> new_prices) {
        List<Double> prices = new ArrayList<>();
        for (NewPrices new_price : new_prices) {
            double share_price_eur = new_price.getNew_price();

            if (new_price.getCurrency().equals("USD")){
                share_price_eur = share_price_eur * getUSD_to_EUR();
            } else{
                share_price_eur = share_price_eur;
            }
            prices.add(share_price_eur);
        }
        return prices;
    }

    // Find what tickers we own
    private static List<String> findNewTickers(List<String> unique_tickers) {
        List<String> answer = new ArrayList<>();
        for (int i = 0; i < unique_tickers.size(); i++) {
            String ticker = unique_tickers.get(i);
            answer.add(ticker);
        }
        return answer;
    }

    // Find how much each of our asset is worth
    private static List<Double> findNewValues(List<String> unique_tickers, List<String> unique_tickers_prices, List<Double> total_volume, List<Double> prices) {
        List<Double> answer = new ArrayList<>();
        for (int i = 0; i < unique_tickers.size(); i++) {
            String ticker = unique_tickers.get(i);
            double volume = total_volume.get(i);
            int new_price_index = unique_tickers_prices.indexOf(ticker);
            double new_price = prices.get(new_price_index);
            double new_value = volume* new_price;
            answer.add(new_value);
        }
        return answer;
    }

    @Override
    public void start(Stage peaLava) throws IOException {
        // ------------------ LISTID JA MUUD MUUTUJAD ------------------ //
        var ref = new Object() { // ref = anonüümne muutuja vist? Igatahes seda on vaja kui lambdat kasutades tahan millelegi väärtuse seada
            String transactionFailinimi = null;
            String pricesFailinimi = null;
        };
        List<Transaction> tehingud = new ArrayList<>();
        List<NewPrices> osakuteHinnad = new ArrayList<>();
        Map<String, Double> osakuteHindadeMap = new HashMap<>();

        List<Text> sõnumid = new ArrayList<>();
        int failistTehingudTekstSuurus, manualTehingudTekstSuurus, tehinguteArvTekst, hindadeArvTekst;

        Text tehinguteArv = new Text("Tehinguid: 0");
        tehinguteArvTekst = tehinguteArv.getText().length()-1;
        Text hindadeArv = new Text("Osakute hindu: 0");
        hindadeArvTekst = hindadeArv.getText().length()-1;

        // --------------- JUUR ------------------
        BorderPane juurpaigutus = new BorderPane();

        // --------------- LEFT PANEL------------------ //
        // 'Sisesta tehingud' plokk
        VBox vbox_tehingute_sisend = new VBox(15);
        Text sisesta_tehingud_peatekst = new Text("Sisesta tehingud"); // tehingute ploki pealkiri
        sisesta_tehingud_peatekst.setFont(Font.font("Arial", FontWeight.BOLD, 18)); // tehingute ploki pealkiri
        Text sisesta_tehingud_pea_kirjeldus = new Text("Võid valida, kas sisestad tehingud failist või ühekaupa."); // lisatekst/kirjeldus
        sisesta_tehingud_pea_kirjeldus.setFont(Font.font("Arial", FontWeight.LIGHT, 10)); // tehingute ploki pealkiri

        // Failist
        VBox vbox_tehingud_failist = new VBox(5);
        Text sisesta_tehingud_failist = new Text("Failist"); // sisesta failinimi
        sisesta_tehingud_failist.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        // tekstiväli ja OK nupp kõrvuti
        HBox hbox_fail_tehingud = new HBox();
        TextField failinimi_tehingud = new TextField("transactions.txt"); // tekstiväli failinimeks
        Button ok_tehingud_failist = new Button("Sisesta"); // sisesta failinimi - programm kontrollib, kas on selline fail. kui ei -> erind
        hbox_fail_tehingud.getChildren().addAll(failinimi_tehingud, ok_tehingud_failist);
        // Veateate tekst + Loetud tekst
        Text veateade_tehingud_fail = new Text("Viga: Faili ei leitud"); // veateade - alguses peidetud
        veateade_tehingud_fail.setFill(Color.RED);
        Text loetud_tehingud_fail = new Text("Andmed sisse loetud!"); // õnnestumine - alguses peidetud
        failistTehingudTekstSuurus = loetud_tehingud_fail.getText().length();
        loetud_tehingud_fail.setFill(Color.GREEN);

        sõnumid.add(veateade_tehingud_fail); // LISAME VEA- JA ÕNNESTUMISTEATED LISTI, ET ME SAAKS NEID KORRAGA ÄRA PEITA [tehingute sõnumid]
        sõnumid.add(loetud_tehingud_fail); // TEHTUD ON 2 ERINEVAT LISTI (1 tehingute oma, 2 hindade oma)
        // lisa kõik VBoxi
        vbox_tehingud_failist.getChildren().addAll(
                sisesta_tehingud_failist,
                hbox_fail_tehingud,
                veateade_tehingud_fail,
                loetud_tehingud_fail
        );
        // NUPUVAJUTUS - Tehingud failist
        ok_tehingud_failist.setOnMouseClicked(event -> {
            try {
                peidaSõnum(veateade_tehingud_fail);
                List<Transaction> tehingutelst = new FileToList(failinimi_tehingud.getText()).readTransactions();
                tehingud.addAll(tehingutelst);
                loetud_tehingud_fail.setText(loetud_tehingud_fail.getText().substring(0,failistTehingudTekstSuurus) + " (" + tehingutelst.size()+")");
                tehinguteArv.setText(tehinguteArv.getText().substring(0,tehinguteArvTekst)+tehingud.size());
                //peidaSõnumid(sõnumid);
                kuva2sek(loetud_tehingud_fail);
                ref.transactionFailinimi = failinimi_tehingud.getText();
            } catch (Exception e) {
                peidaSõnum(loetud_tehingud_fail);
                //peidaSõnumid(sõnumid);
                kuva2sek(veateade_tehingud_fail);
            }
        });

        // Tehingu kaupa
        VBox vbox_tehing_manuaal = new VBox(5);
        Text sisesta_tehing_manual = new Text("Ühekaupa");
        sisesta_tehing_manual.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        Text sisesta_tehing_manual_kirjeldus = new Text("Sisesta tehing kujul:\n(AASTA;BUY/SELL;TICKER;KOGUS;TÜKIHIND;EUR/USD)");
        sisesta_tehing_manual_kirjeldus.setFont(Font.font("Arial", 10));
        HBox hbox_fail_tehing = new HBox();
        TextField tehing_tehingud = new TextField("");
        Button sisesta_tehing_nupp = new Button("Sisesta");
        hbox_fail_tehing.getChildren().addAll(tehing_tehingud, sisesta_tehing_nupp);
        Text veateade_tehing_manual = new Text("Viga: tehingu formaat on vale"); // siia tuleb tekst: kas (1) fiali lugemine õnnestus või (2) erindi tekst
        veateade_tehing_manual.setFill(Color.RED);

        Text loetud_tehing_manual = new Text("Rida sisse loetud!"); // siia tuleb tekst: kas (1) fiali lugemine õnnestus või (2) erindi tekst
        manualTehingudTekstSuurus = loetud_tehing_manual.getText().length();
        loetud_tehing_manual.setFill(Color.GREEN);

        sõnumid.add(veateade_tehing_manual); // [tehingute sõnumid]
        sõnumid.add(loetud_tehing_manual);

        vbox_tehing_manuaal.getChildren().addAll(
                sisesta_tehing_manual,
                sisesta_tehing_manual_kirjeldus,
                hbox_fail_tehing,
                veateade_tehing_manual,
                loetud_tehing_manual);

        // lisa kõik VBoxi
        vbox_tehingute_sisend.getChildren().addAll(
                sisesta_tehingud_peatekst,
                sisesta_tehingud_pea_kirjeldus,
                vbox_tehingud_failist,
                vbox_tehing_manuaal);

        sisesta_tehing_nupp.setOnMouseClicked(event -> {
            try {
                peidaSõnum(veateade_tehing_manual);
                lisaTehing(tehingud,tehing_tehingud.getText());
                //peidaSõnumid(sõnumid);
                loetud_tehing_manual.setText(loetud_tehing_manual.getText().substring(0,manualTehingudTekstSuurus) + " (1)");
                tehinguteArv.setText(tehinguteArv.getText().substring(0,tehinguteArvTekst)+tehingud.size());
                kuva2sek(loetud_tehing_manual);
            } catch (Exception e) {
                peidaSõnum(loetud_tehing_manual);
                //peidaSõnumid(sõnumid);
                kuva2sek(veateade_tehing_manual);
            }
        });

        // 'Sisesta osakute hinnad' plokk
        VBox vbox_hindade_sisend = new VBox(15);
        Text sisesta_hinnad_peatekst = new Text("Sisesta osakute hinnad"); // osakute hindade ploki pealkiri
        sisesta_hinnad_peatekst.setFont(Font.font("Arial", FontWeight.BOLD, 18)); // osakute hindade ploki pealkiri
        Text sisesta_hinnad_pea_kirjeldus = new Text("Võid valida, kas sisestad hinnad failist või ühekaupa."); // lisatekst/kirjeldus
        sisesta_hinnad_pea_kirjeldus.setFont(Font.font("Arial", FontWeight.LIGHT, 10)); // hindade ploki pealkiri

        // Failist
        VBox vbox_hinnad_failist = new VBox(5);
        Text sisesta_hinnad_failist = new Text("Failist"); // sisesta failinimi
        sisesta_hinnad_failist.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        // tekstiväli ja OK nupp kõrvuti
        HBox hbox_fail_hinnad = new HBox();
        TextField failinimi_hinnad = new TextField("prices.txt"); // tekstiväli failinimeks
        Button ok_hinnad_failist = new Button("Sisesta"); // sisesta failinimi - programm kontrollib, kas on selline fail. kui ei -> erind
        hbox_fail_hinnad.getChildren().addAll(failinimi_hinnad, ok_hinnad_failist);
        // Veateate tekst
        Text veateade_hinnad_fail = new Text("Viga: Faili ei leitud"); // siia tuleb tekst: kas (1) fiali lugemine õnnestus või (2) erindi tekst
        veateade_hinnad_fail.setFill(Color.RED);
        Text loetud_hinnad_fail = new Text("Andmed sisse loetud!");
        loetud_hinnad_fail.setFill(Color.GREEN);

        sõnumid.add(veateade_hinnad_fail);  // LISAME VEA- JA ÕNNESTUMISTEATED LISTI, ET ME SAAKS NEID KORRAGA ÄRA PEITA [hindade sõnumid]
        sõnumid.add(loetud_hinnad_fail); // TEHTUD ON 2 ERINEVAT LISTI (1 tehingute oma, 2 hindade oma)

        // lisa kõik VBoxi
        vbox_hinnad_failist.getChildren().addAll(
                sisesta_hinnad_failist,
                hbox_fail_hinnad,
                veateade_hinnad_fail,
                loetud_hinnad_fail
        );

        ok_hinnad_failist.setOnMouseClicked(event -> {
            try {
                peidaSõnum(veateade_hinnad_fail);
                List<NewPrices> hinnad = new FileToList(failinimi_hinnad.getText()).readPrices();
                lisaHinnadMapi(hinnad,osakuteHindadeMap);
                loetud_hinnad_fail.setText(loetud_hinnad_fail.getText().substring(0,failistTehingudTekstSuurus) + " (" + hinnad.size()+")");
                hindadeArv.setText(hindadeArv.getText().substring(0,hindadeArvTekst)+osakuteHindadeMap.size());

                //peidaSõnumid(sõnumid);
                kuva2sek(loetud_hinnad_fail);
                ref.pricesFailinimi = failinimi_hinnad.getText();

            } catch (Exception e) {
                peidaSõnum(loetud_hinnad_fail);
                //peidaSõnumid(sõnumid);
                kuva2sek(veateade_hinnad_fail);
            }
        });

        // Hinna kaupa
        VBox vbox_hind_manuaal = new VBox(5);
        Text sisesta_hind_manual = new Text("Ühekaupa");
        sisesta_hind_manual.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        Text sisesta_hind_manual_kirjeldus = new Text("Sisesta osaku hind kujul:\n(TICKER;HIND;EUR/USD)");
        sisesta_hind_manual_kirjeldus.setFont(Font.font("Arial", 10));
        HBox hbox_fail_hind = new HBox();
        TextField hind_hinnad = new TextField("");
        Button sisesta_hind_nupp = new Button("Sisesta");
        hbox_fail_hind.getChildren().addAll(hind_hinnad, sisesta_hind_nupp);
        Text veateade_hind_manual = new Text("Viga: hinna formaat on vale"); // siia tuleb tekst: kas (1) fiali lugemine õnnestus või (2) erindi tekst
        veateade_hind_manual.setFill(Color.RED);
        Text loetud_hind_manual = new Text("Rida sisse loetud!");
        loetud_hind_manual.setFill(Color.GREEN);

        sõnumid.add(loetud_hind_manual); // [hindade sõnumid]
        sõnumid.add(veateade_hind_manual);

        vbox_hind_manuaal.getChildren().addAll(
                sisesta_hind_manual,
                sisesta_hind_manual_kirjeldus,
                hbox_fail_hind,
                veateade_hind_manual,
                loetud_hind_manual);

        // lisa kõik VBoxi
        vbox_hindade_sisend.getChildren().addAll(
                sisesta_hinnad_peatekst,
                sisesta_hinnad_pea_kirjeldus,
                vbox_hinnad_failist,
                vbox_hind_manuaal);

        sisesta_hind_nupp.setOnMouseClicked(event -> {
            try {
                peidaSõnum(veateade_hind_manual);
                List<NewPrices> värk = new ArrayList<>();

                lisaHind(osakuteHindadeMap, hind_hinnad.getText());
                //peidaSõnumid(sõnumid);
                loetud_hind_manual.setText(loetud_hind_manual.getText().substring(0,manualTehingudTekstSuurus) + " (1)");
                hindadeArv.setText(hindadeArv.getText().substring(0,hindadeArvTekst)+osakuteHindadeMap.size());
                kuva2sek(loetud_hind_manual);

            } catch (Exception e) {
                peidaSõnum(loetud_hind_manual);
                //peidaSõnumid(sõnumid);
                kuva2sek(veateade_hind_manual);
            }
        });

        VBox tehingud_hinnad = new VBox(20);
        tehingud_hinnad.getChildren().addAll(vbox_tehingute_sisend,vbox_hindade_sisend);

        // Vboxi paigutus ja stseenile lisamine
        juurpaigutus.setLeft(tehingud_hinnad);
        juurpaigutus.setAlignment(tehingud_hinnad, Pos.TOP_LEFT);
        juurpaigutus.setMargin(tehingud_hinnad, new Insets(50,12,12,12)); // optional

        // ---------------CENTER PANEL------------------ //

        VBox vbox_tulemused = new VBox(10);
        Text tulemused_pealkiri = new Text("\n\nPortfelli ülevaade");
        tulemused_pealkiri.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        // Center panel nupud
        Button arvuta_tulemused_nupp = new Button("Näita portfelli!"); // nupp portfelli tulemuste näitamiseks
        Button restart_program_nupp = new Button("Reset");
        HBox hbox_center_buttons = new HBox(100);
        hbox_center_buttons.getChildren().addAll(arvuta_tulemused_nupp, restart_program_nupp);

        // Ülevaade tehingute ja erinevate osakutehindade kohta
        HBox ülevaade = new HBox();
        ülevaade.setSpacing(30);
        ülevaade.getChildren().addAll(tehinguteArv,hindadeArv);

        // Tulemuste tekstid
        Text tehingute_summa = new Text("Portfelli ülevaate nägemiseks sisesta andmed.");
        tehingute_summa.setFill(Color.RED);
        Text unikaalsed_osakud_arv = new Text(""); // Unikaalsete osakute arv
        HBox hbox_tulemused_joonis = new HBox(5);
        VBox lisainfo = new VBox();

        // Alusta programmi uuesti
        restart_program_nupp.setOnMouseClicked(mouseEvent -> {
            osakuteHindadeMap.clear();
            tehingud.clear();
            arvuta_tulemused_nupp.fireEvent(mouseEvent);
            tehinguteArv.setText(tehinguteArv.getText().substring(0,tehinguteArvTekst)+"0");
            hindadeArv.setText(hindadeArv.getText().substring(0,hindadeArvTekst)+"0");
            lisainfo.getChildren().clear();
            tehingute_summa.setText("Portfelli ülevaate nägemiseks sisesta andmed.");
            tehingute_summa.setFill(Color.RED);
            peidaSõnum(unikaalsed_osakud_arv);

        } );

        arvuta_tulemused_nupp.setOnMouseClicked(event -> {
            // TO DO: kontrolli, et andmed ei oleks duplikeeritud!
            try {
                hbox_tulemused_joonis.getChildren().clear();
                lisainfo.getChildren().clear();
                if (tehingud.size() == 0 || osakuteHindadeMap.size() == 0) throw new Exception(); // kui üks listidest on tühi, siis ei arvuta midagi
                // Lets find unique tickers and how much of said asset do we own
                List<String> unique_tickers = findUniqueTickers(tehingud);
                List<Double> total_volume = findTotalVolume(tehingud);

                double koguVäärtus = 0.00d;
                for (int i = 0; i < unique_tickers.size(); i++) {
                    String ticker = unique_tickers.get(i); // unique ticker
                    double volume_ticker = total_volume.get(i); // unique ticker's total volume
                    double new_price = osakuteHindadeMap.get(ticker); // get the new price
                    double ticker_value_eur = volume_ticker * new_price; // compute the total cvalue of a unique ticker
                    koguVäärtus = koguVäärtus + ticker_value_eur;
                }

                VBox vbox_tickerid = new VBox(5);
                VBox vbox_tulbad = new VBox(5);
                VBox vbox_osakaalud = new VBox(5);
                VBox vbox_nominaalsummad = new VBox(5);
                List<OsakuteOsakaal> osakaaludeList = new ArrayList<>();

                for (int i = 0; i < unique_tickers.size(); i++) {
                    HBox hbox_tulp_info = new HBox(5);
                    String ticker = unique_tickers.get(i); // unique ticker
                    double volume_ticker = total_volume.get(i); // unique ticker's total volume
                    double new_price = osakuteHindadeMap.get(ticker); // get the new price
                    double ticker_value_eur = volume_ticker * new_price; // compute the total cvalue of a unique ticker
                    double proportion = ticker_value_eur / koguVäärtus; // get the proportion of new ticker's value in total portfolio value
                    ticker_value_eur = Math.round(ticker_value_eur * 100) / 100.00;
                    osakaaludeList.add(new OsakuteOsakaal(ticker, ticker_value_eur, proportion));
                }

                Collections.sort(osakaaludeList);

                int lugeja = 0;
                OsakuteOsakaal muud = new OsakuteOsakaal("*ülejäänud",0,0);
                for (OsakuteOsakaal osakuteOsakaal : osakaaludeList) {
                    lugeja += 1;
                    if (lugeja>8) {
                        muud.lisaHinda(osakuteOsakaal.getPraeguneEurHind());
                        muud.lisaProportsiooni(osakuteOsakaal.getProportsioon());
                        if (lugeja != osakaaludeList.size()) continue;
                    }
                    if (lugeja>8) {
                        osakuteOsakaal = muud;
                    }
                   // String ticker_bar = returnStringPlot(ticker, proportion, ticker_value_eur, 20); // kui kasutada vana versiooni meetodit

                    // Andmed jooniseks
                    Text ticker_text = new Text(osakuteOsakaal.getOsakuNimi() + "\n");
                    Rectangle ticker_bar = new Rectangle(osakuteOsakaal.getProportsioon() * 300, 30); // tulp, mille kõrgus sõltub väärtusest; vt, vb peab 10 asemel midagi muud kasutama
                   ticker_bar.setFill(Color.DARKGRAY);
                    // Text ticker_osakaal = new Text(proportion + "%");
                    Text ticker_nominaalsumma = new Text("(" + osakuteOsakaal.getPraeguneEurHind() + " EUR)"+ "\n");
                    Text ticker_osakaal = new Text((Math.round(osakuteOsakaal.getProportsioon()*10000))/100.00 + "%"+ "\n");
                  //  ticker_text.setFont(Font.font("Arial", FontWeight.BOLD, 10));
                    vbox_tickerid.getChildren().add(ticker_text);
                    vbox_tulbad.getChildren().add(ticker_bar);
                    vbox_osakaalud.getChildren().add(ticker_osakaal);
                    vbox_nominaalsummad.getChildren().add(ticker_nominaalsumma);
                }

                hbox_tulemused_joonis.getChildren().addAll(vbox_tickerid, vbox_tulbad, vbox_osakaalud, vbox_nominaalsummad);
                //  double koguVäärtus = (double) (Math.round((totalAmountBought(tehingud) - totalAmountSold(tehingud))*1000))/1000;
                tehingute_summa.setText("\n\nPortfelli väärtus on kokku: " + Math.round(koguVäärtus*100)/100.00 + " EUR.");
                tehingute_summa.setFill(Color.BLACK);
                tehingute_summa.setManaged(true); tehingute_summa.setVisible(true);

                // Portfellis olevate unikaalsete osakute arv
                unikaalsed_osakud_arv.setText("Unikaalseid osakuid portfoolios: " + osakaaludeList.size() + "\n"+ "\n"); // osakuteHindadeMap.size() väljendab sama, mis unikaalsete osakute arv
                unikaalsed_osakud_arv.setManaged(true); unikaalsed_osakud_arv.setVisible(true);

                // Lisainfo
                double ostuvolüüm = totalAmountBought(tehingud);
                double müügivolüüm = totalAmountSold(tehingud);
                double pnlväärtus = Math.round((koguVäärtus-(ostuvolüüm-müügivolüüm))*100.0)/100.0;
                String pnlstring= pnlväärtus>0?"+"+pnlväärtus:""+pnlväärtus;
                Text kokkuOstetud = new Text("Ostuvolüüm: " + Math.round(ostuvolüüm*100.0)/100.0 + "€");
                Text kokkuMüüdud = new Text("Müügivolüüm: " + Math.round(müügivolüüm*100)/100.0 + "€");
                Text pnl = new Text("PnL: " + pnlstring + "€");
                if (pnlväärtus>0) pnl.setFill(Color.GREEN);
                else pnl.setFill(Color.RED);
                lisainfo.getChildren().addAll(kokkuOstetud,kokkuMüüdud,pnl);

            } catch (Exception e) {
            }
        });

        // Lisa kast, kuhu kuvatakse tulemused
     //   Rectangle tulemuste_kast = new Rectangle();
     //   tulemuste_kast.setFill(Color.MINTCREAM); // tulemuste kasti värv
     //   tulemuste_kast.widthProperty().bind(vbox_tulemused.widthProperty().subtract(100)); // tulemuste kasti dünaamiline laius
      //  tulemuste_kast.heightProperty().bind(vbox_tulemused.heightProperty().subtract(100)); // tulemuste kasti dünaamiline kõrgus

        vbox_tulemused.getChildren().addAll(tulemused_pealkiri, ülevaade,
                hbox_center_buttons,
                tehingute_summa, unikaalsed_osakud_arv,
                hbox_tulemused_joonis,
                lisainfo);
        vbox_tulemused.setSpacing(0);
        juurpaigutus.setCenter(vbox_tulemused);

        // --------------- RIGHT PANEL------------------ //
        // Programmi kasutamise juhised
        VBox vbox_kasutusjuhend = new VBox(10);
        Text kasutusjuhend_pealkiri = new Text("\n\n\n\n\nProgrammi kasutamine:");
        kasutusjuhend_pealkiri.setFont(Font.font("Arial", FontWeight.BOLD, 15)); // tehingute ploki
        Text kasutusjuhend_tekst = new Text("Programm võtab sisendiks \ntehingud ja osakute hinnad\n" +
                "Mõlemad saab sisse lugeda \nkas failist või käsitsi ühekaupa lisades.\n" +
                "Kui faili ei leita või \nformaat on vale, saab veateate.\n" +
                "Kui andmetega on kõik korras, \nväljastab programm info portfelli kohta.\n" +
                "Tulemused saab ka salvestada.");
        kasutusjuhend_tekst.setFont(Font.font("Arial",12));
        vbox_kasutusjuhend.getChildren().addAll(kasutusjuhend_pealkiri, kasutusjuhend_tekst);

        // Salvesta nupp
        VBox vbox_parem_paneel = new VBox(15);
        VBox vbox_salvesta = new VBox(); // VBox salvestamise mooduli jaoks
        Text salvesta_tekst_pealkiri = new Text("Salvesta andmed"); // Salvestamise pealkiri
        salvesta_tekst_pealkiri.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        Text salvesta_tekst = new Text("Salvesta uuendatud tehingute ja hindade failid.");
        salvesta_tekst.setFont(Font.font("Arial", 12));

        Button salvesta_nupp =  new Button("Salvesta"); // Salvestamise nupp
        Text hea_salvesta_tagasiside = new Text(); // Salvestuse järgne tekst
        hea_salvesta_tagasiside.setText("Failid edukalt salvestatud!");
        hea_salvesta_tagasiside.setFill(Color.GREEN);
        Text halb_salvesta_tagasiside = new Text();
        halb_salvesta_tagasiside.setText("Ei suutnud faile salvestada");
        halb_salvesta_tagasiside.setFill(Color.RED);

        sõnumid.add(hea_salvesta_tagasiside);
        sõnumid.add(halb_salvesta_tagasiside);
        // juurpaigutus.setMargin(vbox_salvesta, new Insets(12,12,12,12)); // optional

        salvesta_nupp.setOnMouseClicked(event -> {
            try {
                peidaSõnum(halb_salvesta_tagasiside);
                if (tehingud.size() == 0 || osakuteHindadeMap.size() == 0) throw new Exception(); // kui üks listidest on tühi, siis ei salvesta faile
                salvestaTehingudFaili(tehingud, "uus_tehingud.txt");
                salvestaHinnadFaili(osakuteHindadeMap, "uus_hinnad.txt");

                //peidaSõnumid(sõnumid);
                kuva2sek(hea_salvesta_tagasiside);
            } catch (Exception e) {
                peidaSõnum(hea_salvesta_tagasiside);
                //peidaSõnumid(sõnumid);
                kuva2sek(halb_salvesta_tagasiside);
            }
        });

        // Sulgemise nupp
        Button sulge_nupp = new Button("Sulge programm");
        vbox_salvesta.setSpacing(20);
        vbox_salvesta.getChildren().addAll(salvesta_tekst_pealkiri,salvesta_tekst, salvesta_nupp, hea_salvesta_tagasiside, halb_salvesta_tagasiside, sulge_nupp);
        sulge_nupp.setOnMouseClicked(event -> Platform.exit()); // nupu vajutamisel sulgub programm
        // vbox_parem_paneel.setSpacing(60);
        vbox_parem_paneel.getChildren().addAll(vbox_kasutusjuhend,vbox_salvesta);
        vbox_parem_paneel.setPrefWidth(70);
        juurpaigutus.setMargin(vbox_parem_paneel, new Insets(0,12,0,0));
        juurpaigutus.setRight(vbox_parem_paneel);


        // ---------------TOP PANEL------------------ //
        // Logo-tekst
        Text logotekst = new Text("Portfellihaldur");
        logotekst.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        juurpaigutus.setMargin(logotekst, new Insets(12,12,12,12)); // optional
        juurpaigutus.setAlignment(logotekst, Pos.CENTER);
        juurpaigutus.setTop(logotekst);

        // ---------------BOTTOM PANEL------------------ //
        // Disclaimer
        Text disclaimer_tekst = new Text("Lahtiütleja. Tegemist ei ole investeerimissoovitusega.\nProgramm on meelelahutusliku sisuga ning selles kajastatu ei pruugi tugineda kaasajastatud informatsioonil.\nEnne programmi kasutamist investeerimiseks või muudeks sellega seonduvateks tegevusteks konsulteeri vastava ala spetsialistiga.\n\nProgrammi autorid: Dmitri Rozgonjuk ja Richard Prost");
        disclaimer_tekst.setFont(Font.font("Arial", 10));
        juurpaigutus.setMargin(disclaimer_tekst, new Insets(12,12,12,12));
        disclaimer_tekst.setTextAlignment(TextAlignment.CENTER);
        juurpaigutus.setAlignment(disclaimer_tekst, Pos.CENTER);
        juurpaigutus.setBottom(disclaimer_tekst);

        // ---------------SCENE AND MAIN STAGE------------------ //
        // Aken
        peidaSõnumid(sõnumid); peidaSõnumid(sõnumid);// ALGUSES PEIDAME KÕIK ERROR JA ÕNNESTUMISE SÕNUMID ÄRA
        Scene scene = new Scene(juurpaigutus, 980, 700);
        peaLava.setScene(scene);
        peaLava.setResizable(true);
        peaLava.setTitle("Portfellihaldur v0.01");
        peaLava.show();

    }

    // ---------------------------------------------------------------------------------- //
    // -------------------------------------- KÄIVITA --------------------------------------
    public static void main(String[] args) {
        launch();
    }
    // ---------------------------------------------------------------------------------- //

// Kui on soov kasutada StringPlot lahendust:
    public String returnStringPlot(String ticker, double proportion, double ticker_value_eur, int bar_length){
        int n_hashtags = (int) ((proportion)*(bar_length)); // Compute the number of hashtags (bar plot)
        proportion = (Math.round(proportion*10000))/100.00;
        ticker_value_eur = (Math.round(ticker_value_eur*100))/100.00;
        String ticker_sout; // for better alignment, add some spaces to ticker (if less than 7 characters)
        if (ticker.length() < 7){
            ticker_sout = ticker + " ".repeat(7 - ticker.length());
        } else{
            ticker_sout = ticker;
        }
        // The "plot"
        return "|" + "#".repeat(n_hashtags) + "-".repeat(bar_length-n_hashtags) + "|" + " "
                + ticker_sout + proportion + "% " + "(" + ticker_value_eur + " EUR)";
    }


}