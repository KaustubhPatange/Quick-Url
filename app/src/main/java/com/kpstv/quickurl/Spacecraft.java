package com.kpstv.quickurl;



/**
 * Created by kp on 19/7/17.
 */

public class Spacecraft {
    private String title, genre, year;

public Spacecraft(String longurl, String shorturl, String year){
    this.title = longurl;
    this.genre = shorturl;
    this.year = year;
}

    public String getTitle() {
        return title;
    }

    public void setTitle(String name) {
        this.title = name;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}