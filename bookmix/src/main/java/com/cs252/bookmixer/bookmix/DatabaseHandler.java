package com.cs252.bookmixer.bookmix;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by viraj on 5/5/14.
 */
public class DatabaseHandler extends SQLiteOpenHelper {
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "booksManager";

    // Books table name
    private static final String TABLE_BOOKS = "books";

    // Books Table Columns names
    private static final String KEY_ID = "id"; // primary key
    private static final String KEY_TITLE = "title";
    private static final String KEY_AUTHOR = "author";
    private static final String KEY_YEAR = "year";
    private static final String KEY_NUMBER = "number"; // number out of proj. gutenberg's top 100
    private static final String KEY_DOWNLOADED = "is_downloaded"; // whether or not the book is dl'd
    private static final String KEY_TEXT = "text"; // the book file itself

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_BOOKS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_TITLE + " VARCHAR,"
                + KEY_AUTHOR + " VARCHAR,"
                + KEY_YEAR + " VARCHAR,"
                + KEY_NUMBER + " INTEGER,"
                + KEY_DOWNLOADED + " BIT,"
                + KEY_TEXT + " TEXT"
                + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    public void addBook(Book book) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, book.get_id());
        values.put(KEY_TITLE, book.get_title());
        values.put(KEY_AUTHOR, book.get_author());
        values.put(KEY_YEAR, book.get_year());
        values.put(KEY_NUMBER, book.get_number());
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
        db.close(); // Closing database connection
    }

    public Book getBook(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_BOOKS, new String[] { KEY_ID,
                KEY_TITLE, KEY_AUTHOR, KEY_YEAR, KEY_NUMBER, KEY_DOWNLOADED, KEY_TEXT  },
                KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();


        Book book = new Book(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                Integer.parseInt(cursor.getString(4)),
                cursor.getString(5));
        // return contact
        return book;
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);

        // Create tables again
        onCreate(db);
    }
}
