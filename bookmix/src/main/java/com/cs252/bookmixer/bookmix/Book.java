package com.cs252.bookmixer.bookmix;

/**
 * Created by viraj on 5/5/14.
 */
public class Book {
    int _id; // primary db key
    String _title;
    String _author;
    String _URL;
    boolean is_downloaded;
    String _text;

    // if we have the string that is the book text already
    public Book(int id, String title, String author, String URL, String text) {
        this._id = id;
        this._title = title;
        this._author = author;
        this._URL = URL;

        if (text != null) {
            this._text = text;
            is_downloaded = true;
        } else {
            this._text = null;
            is_downloaded = false;
        }
    }

    // if we don't have the text
    public Book(int id, String title, String author, String URL) {
        this._id = id;
        this._title = title;
        this._author = author;
        this._URL = URL;
        this._text = null;
        is_downloaded = false;
    }

    public String toString() {
        return _title + " (" + _author + ")";
    }

    public String getURL() {
        return _URL;
    }

    public boolean is_downloaded() {
        return !_text.equals("");
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

    public String get_text() {
        return _text;
    }

    public void set_text(String _text) {
        this._text = _text;
    }
}