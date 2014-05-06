package com.cs252.bookmixer.bookmix;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by viraj on 5/5/14.
 */
public class DatabaseAdapter {

    // Books Table Columns names
    private static final String KEY_ID = "_id"; // primary key
    private static final String KEY_TITLE = "KEY_TITLE";
    private static final String KEY_AUTHOR = "KEY_AUTHOR";
    private static final String KEY_URL = "KEY_URL";
    private static final String KEY_DOWNLOADED = "KEY_DOWNLOADED"; // whether or not the book is dl'd
    private static final String KEY_TEXT = "KEY_TEXT"; // the book file itself

    protected static final String TAG = "DataAdapter";
    private static final String TABLE_BOOKS = "Books";

    private final Context mContext;
    private SQLiteDatabase mDb;
    private DatabaseHandler dbHandler;

    public DatabaseAdapter(Context context) {
        this.mContext = context;
        dbHandler = new DatabaseHandler(mContext);
    }

    public Book getBook(int id) {
        this.open();
        Cursor cursor = mDb.query(TABLE_BOOKS, new String[] { KEY_ID,
                KEY_TITLE, KEY_AUTHOR, KEY_URL, KEY_DOWNLOADED, KEY_TEXT  },
                KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }

        Book book = new Book(Integer.parseInt(cursor.getString(0)), // id
                cursor.getString(1), //title
                cursor.getString(2), // author
                cursor.getString(3), // url
                cursor.getString(5)); // text (if any)

        // return contact
        return book;
    }

    public List<Book> getAllBooks() {
        List<Book> bookList = new ArrayList<Book>();
        String selectQuery = "SELECT * FROM " + TABLE_BOOKS;
        SQLiteDatabase db = dbHandler.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Book book = getBook(Integer.parseInt(cursor.getString(0)));
                bookList.add(book);
            } while (cursor.moveToNext());
        }
        db.close();

        // return contact list
        return bookList;
    }

    public void addBook(Book book) {
        Log.v(TAG, "adding books");
        SQLiteDatabase db = dbHandler.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, book.get_id());
        values.put(KEY_TITLE, book.get_title());
        values.put(KEY_AUTHOR, book.get_author());
        values.put(KEY_DOWNLOADED, book.is_downloaded());
        if (book.is_downloaded()) {
            values.put(KEY_TEXT, book.get_text());
        }

        // Inserting Row
        try {
            db.insert(TABLE_BOOKS, null, values);
        } catch (SQLiteConstraintException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "closing writable db after adding book");
        db.close(); // close database connection
    }

    // update book in db with text post-download
    public void updateBook(Book book) {
        SQLiteDatabase db = dbHandler.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, book.get_id());
        values.put(KEY_TITLE, book.get_title());
        values.put(KEY_AUTHOR, book.get_author());
        values.put(KEY_DOWNLOADED, book.is_downloaded());
        if (book.is_downloaded()) {
            values.put(KEY_TEXT, book.get_text());
        }

        db.update(TABLE_BOOKS, values, "_id " + "=" + book.get_id(), null);

        Log.d(TAG, "finished updating book: " + book.get_title());
        db.close();
    }

    public DatabaseAdapter createDatabase() throws SQLException
    {
        try {
            dbHandler.createDataBase();
        }
        catch (IOException mIOException) {
            Log.e(TAG, mIOException.toString() + "  UnableToCreateDatabase");
            throw new Error("UnableToCreateDatabase");
        }
        return this;
    }

    public DatabaseAdapter open() throws SQLException {
        try {
            dbHandler.openDataBase();
            dbHandler.close();
            mDb = dbHandler.getReadableDatabase();
        } catch (SQLException mSQLException) {
            Log.e(TAG, "open >>"+ mSQLException.toString());
            throw mSQLException;
        }
        return this;
    }

    public void close() {
        Log.d(TAG, "closing db");
        dbHandler.close();
    }

    public void resetDB() {
        dbHandler.resetDB();
    }
}