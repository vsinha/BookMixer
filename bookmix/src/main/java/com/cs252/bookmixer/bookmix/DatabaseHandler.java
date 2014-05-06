package com.cs252.bookmixer.bookmix;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by viraj on 5/5/14.
 */
public class DatabaseHandler extends SQLiteOpenHelper {
    // All Static variables
    private static final int DATABASE_VERSION = 1;
    private static String TAG = "DataBaseHelper";
    private static final String DB_NAME = "booksManager.db";
    private static final String TABLE_BOOKS = "Books";
    private static String DB_PATH = "";
    private final Context mContext;
    private SQLiteDatabase mDataBase;

    // Books Table Columns names
    private static final String KEY_ID = "_id"; // primary key
    private static final String KEY_TITLE = "KEY_TITLE";
    private static final String KEY_AUTHOR = "KEY_AUTHOR";
    private static final String KEY_URL = "KEY_URL";
    private static final String KEY_DOWNLOADED = "KEY_DOWNLOADED"; // whether or not the book is dl'd
    private static final String KEY_TEXT = "KEY_TEXT"; // the book file itself

    public DatabaseHandler(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);

        if(android.os.Build.VERSION.SDK_INT >= 17){
            Log.d(TAG, "using dir: " + context.getApplicationInfo().dataDir);
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        } else {
            DB_PATH = "/data/data/" + context.getPackageName().toString() + "/databases/";
            Log.d(TAG, "using dir: " + DB_PATH);
        }
        this.mContext = context;
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        /*
        System.out.println("creating DB");
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_BOOKS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_TITLE + " VARCHAR,"
                + KEY_AUTHOR + " VARCHAR,"
                + KEY_URL + " VARCHAR, "
                + KEY_DOWNLOADED + " BIT,"
                + KEY_TEXT + " TEXT"
                + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
        */
    }

    public void createDataBase() throws IOException {
        //If database not exists copy it from the assets
        boolean mDataBaseExist = checkDataBase();
        if(!mDataBaseExist) {
            Log.e(TAG, "attempting to copy databse");
            this.getReadableDatabase();
            this.close();
            try {
                //Copy the database from assets
                copyDataBase();
                Log.e(TAG, "createDatabase database created");
            } catch (IOException mIOException) {
                throw new Error("ErrorCopyingDataBase");
            }
        }
    }

    //Copy the database from assets
    private void copyDataBase() throws IOException {
        Log.d(TAG, "attempting to copy");

        InputStream mInput = mContext.getAssets().open(DB_NAME);
        Log.d(TAG, "opened db successfully");
        String outFileName = DB_PATH + DB_NAME;
        OutputStream mOutput = new FileOutputStream(outFileName);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer)) > 0) {
            mOutput.write(mBuffer, 0, mLength);
        }
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

    //Check that the database exists here: /data/data/your package/databases/Da Name
    private boolean checkDataBase() {
        File dbFile = new File(DB_PATH + DB_NAME);
        Log.v("dbFile", dbFile + "   "+ "Does db exist? " + dbFile.exists());
        return dbFile.exists();
    }

    //Open the database, so we can query it
    public boolean openDataBase() throws SQLException {
        String mPath = DB_PATH + DB_NAME;
        //Log.v("mPath", mPath);
        mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        //mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        return mDataBase != null;
    }

    public void addBook(Book book) {
        Log.v(TAG, "adding books");
        SQLiteDatabase db = this.getWritableDatabase();

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
        db.close(); // Closing database connection
    }

    public Book getBook(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_BOOKS, new String[] { KEY_ID,
                KEY_TITLE, KEY_AUTHOR, KEY_URL, KEY_DOWNLOADED, KEY_TEXT  },
                KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        /*
            CREATE TABLE Books(
            KEY_ID INTEGER PRIMARY KEY,
            KEY_TITLE VARCHAR,
            KEY_AUTHOR VARCHAR,
            KEY_URL VARCHAR,
            KEY_DOWNLOADED BIT,
            KEY_TEXT TEXT )
         */
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
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Book book = getBook(Integer.parseInt(cursor.getString(0)));
                bookList.add(book);
            } while (cursor.moveToNext());
        }

        // return contact list
        return bookList;
    }

    @Override
    public synchronized void close()     {
        if(mDataBase != null)
            mDataBase.close();
        super.close();
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);

        // Create tables again
        onCreate(db);
        */
    }

    public void resetDB() {
        File dbFile = new File(DB_PATH + DB_NAME);
        Log.v("dbFile", dbFile + "   "+ "Does db exist? " + dbFile.exists());
        boolean deleted = dbFile.delete();

        Log.d(TAG, "resetting the DB (was previously deleted? " + deleted);
        Log.d(TAG, "does it still exist?: " + checkDataBase());

        /*
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);


        // Create tables again
        try {
            createDataBase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }
}