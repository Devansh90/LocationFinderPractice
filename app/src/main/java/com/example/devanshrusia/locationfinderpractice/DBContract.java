package com.example.devanshrusia.locationfinderpractice;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by devanshrusia on 6/13/16.
 */
public final class DBContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public DBContract() {
    }

    /* Inner class that defines the table contents */
    public static abstract class DBEntry implements BaseColumns {
        public static final String TABLE_NAME = "LocData";
        public static final String COLUMN_NAME_WIFI_NAME = "wifiId";
        public static final String COLUMN_NAME_LAT = "lat";
        public static final String COLUMN_NAME_LONG = "long";
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String DOUBLE_TYPE = " REAL";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + DBEntry.TABLE_NAME + " (" +
                    DBEntry._ID + " INTEGER PRIMARY KEY," +
                    DBEntry.COLUMN_NAME_WIFI_NAME + TEXT_TYPE + COMMA_SEP +
                    DBEntry.COLUMN_NAME_LAT + DOUBLE_TYPE + COMMA_SEP +
                    DBEntry.COLUMN_NAME_LONG + DOUBLE_TYPE +
                    " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + DBEntry.TABLE_NAME;

    public static final class DBReaderHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "Reader.db";

        public DBReaderHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_DELETE_ENTRIES);
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }
}
