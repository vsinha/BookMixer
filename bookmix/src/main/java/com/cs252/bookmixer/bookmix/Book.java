package com.cs252.bookmixer.bookmix;

/**
 * Created by viraj on 5/5/14.
 */
public class Book {
    int _id; // primary db key
    String _title;
    String _author;
    String _year;
    int _number;
    String _text;
    boolean is_downloaded;


    // if we have the string that is the book text already
    public Book(int id, String title, String author, String year, int number, String text) {
        this._id = id;
        this._title = title;
        this._author = author;
        this._year = year;
        this._number = number;
        this._text = text;
        is_downloaded = true;
    }

    // if we don't have the text
    public Book(int id, String title, String author, String year, int number) {
        this._id = id;
        this._title = title;
        this._author = author;
        this._year = year;
        this._number = number;
        this._text = null;
        is_downloaded = false;
    }

    public String toString() {
        return _title + " (" + _author + ", " + _year + ")";
    }

    public boolean is_downloaded() {
        return is_downloaded;
    }

    public void set_downloaded(boolean is_downloaded) {
        this.is_downloaded = is_downloaded;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
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

    public String get_year() {
        return _year;
    }

    public void set_year(String _year) {
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
