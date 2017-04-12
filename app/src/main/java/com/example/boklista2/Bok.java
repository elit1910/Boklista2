package com.example.boklista2;



/**
 * Created by estlander on 03/04/2017.
 */

public class Bok {

    private String titel;
    private String[] forfattare;
    private String sammanfattning;
    private String bokLank;


    public Bok(String titeli, String[] krijoittaja, String yhteenveto, String bokLinkki){

        titel = titeli;
        forfattare = krijoittaja;
        sammanfattning = yhteenveto;
        bokLank = bokLinkki;
    }

    public String getTitle(){
        return titel;
    }



    public String[] getForfattare() {
        return forfattare;
    }

    public String generateStringOfAuthor() {
        String s = "";
        for(int i=0;i<forfattare.length;i++) {
            if(i == forfattare.length-1)
                s += forfattare[i];
            else
                s += forfattare[i] + ", ";
        }
        return s;
    }

    public String getSammanfattning(){
        return sammanfattning;
    }

    public String getBokLank(){
        return bokLank;
    }

}
