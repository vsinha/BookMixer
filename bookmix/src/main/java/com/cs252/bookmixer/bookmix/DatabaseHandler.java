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

    @Override
    public synchronized void close() {
        if(mDataBase != null)
            mDataBase.close();
        super.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    // delete the stored file so we recopy from assets/
    public void resetDB() {
        File dbFile = new File(DB_PATH + DB_NAME);
        Log.v("dbFile", dbFile + "   "+ "Does db exist? " + dbFile.exists());
        boolean deleted = dbFile.delete();

        Log.d(TAG, "resetting the DB (was previously deleted? " + deleted);
        Log.d(TAG, "does it still exist?: " + checkDataBase());
    }
}