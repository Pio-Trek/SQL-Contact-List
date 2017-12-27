package com.sundaydevblog.sqlcontactlist.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sundaydevblog.sqlcontactlist.data.DatabaseContract.ContactEntry;

/**
 * Database helper - manages database creation and version management.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "contacts.db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the contacts table
        String SQL_CREATE_DB_TABLE = "CREATE TABLE " + ContactEntry.TABLE_NAME + " ("
                + ContactEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ContactEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + ContactEntry.COLUMN_ADDRESS + " TEXT, "
                + ContactEntry.COLUMN_PHONE + " TEXT NOT NULL, "
                + ContactEntry.COLUMN_PICTURE + " BLOB);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_DB_TABLE);

    }

    /**
     * Called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.

    }
}
