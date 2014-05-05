package com.cs252.bookmixer.bookmix;

/**
 * Created by viraj on 5/5/14.
 */
public class Book {
    String _title;
    String _author;
    int _year;
    int _number;
    String _text;
    boolean is_downloaded;

    public Book(String title, String author, int year, int number, String text) {
        this._title = title;
        this._author = author;
        this._year = year;
        this._number = number;
        this._text = text;
        is_downloaded = false;
    }

    public String get_title() {
        return _title;
    }

    public void set_title(String _title) {
        this._title = _title;
    }

    public String get_author() {
        return _author;
    }

    public void set_author(String _author) {
        this._author = _author;
    }

    public int get_year() {
        return _year;
    }

    public void set_year(int _year) {
        this._year = _year;
    }

    public int get_number() {
        return _number;
    }

    public void set_number(int _number) {
        this._number = _number;
    }

    public String get_text() {
        return _text;
    }

    public void set_text(String _text) {
        this._text = _text;
    }
}
