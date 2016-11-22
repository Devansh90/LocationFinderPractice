package com.example.devanshrusia.locationfinderpractice;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MerchantLocation {

    DBContract.DBReaderHelper mDbHelper;

    public void init(Context context) {
        mDbHelper = new DBContract.DBReaderHelper(context);
    }

    public void setupDb() {
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        mDbHelper.onCreate(db);
        insertIntoDb(db, "titan", 12.000, 12.0000);
        insertIntoDb(db, "tanishq", 12, 12);
        insertIntoDb(db, "ola", 12, 12);
        insertIntoDb(db, "ccd", 12, 12);
    }

    private void insertIntoDb(SQLiteDatabase db, String wifiName, double lat, double lng) {
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DBContract.DBEntry.COLUMN_NAME_WIFI_NAME, wifiName);
        values.put(DBContract.DBEntry.COLUMN_NAME_LAT, lat);
        values.put(DBContract.DBEntry.COLUMN_NAME_LONG, lng);

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                DBContract.DBEntry.TABLE_NAME,
                null,
                values);

        Log.i("LocFinder", "Insert complete for row : " + newRowId + " name: " + wifiName);
    }

    public void fetchMerchant(String wifiSSID) {
        Log.i("LocFinder","Looking for wifi Data");
        wifiSSID = wifiSSID.toLowerCase();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + DBContract.DBEntry.TABLE_NAME + " where " + DBContract.DBEntry.COLUMN_NAME_WIFI_NAME + " = " + wifiSSID;

//        Cursor cursor = db.rawQuery(selectQuery, null);
//
//        Log.i("LocFinder","DB query run complete");
//        // looping through all rows and adding to list
//        if (cursor.moveToFirst()) {
//            Log.i("LocFinder","Found DB data");
//            long itemId = cursor.getLong(
//                    cursor.getColumnIndexOrThrow(DBContract.DBEntry._ID)
//            );
//            String wifiName = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.DBEntry.COLUMN_NAME_WIFI_NAME));
//            String wifiLat = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.DBEntry.COLUMN_NAME_LAT));
//            String wifiLong = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.DBEntry.COLUMN_NAME_LONG));
//
//            Log.i("LocFinder", "Read db, id : " + itemId + " Wifi " + wifiName + " lat: " + wifiLat + " long:" + wifiLong);
//        }
//        cursor.close();
    }

    private void dbReader() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

//        // Define a projection that specifies which columns from the database
//        // you will actually use after this query.
//        String[] projection = {
//                DBContract.DBEntry._ID,
//                DBContract.DBEntry.COLUMN_NAME_WIFI_NAME,
//                DBContract.DBEntry.COLUMN_NAME_LAT,
//                DBContract.DBEntry.COLUMN_NAME_LONG
//        };

//        // How you want the results sorted in the resulting Cursor
//        String sortOrder =
//                DBContract.DBEntry.COLUMN_NAME_WIFI_NAME + " DESC";

        // Select All Query
        String selectQuery = "SELECT  * FROM " + DBContract.DBEntry.TABLE_NAME;

        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                long itemId = cursor.getLong(
                        cursor.getColumnIndexOrThrow(DBContract.DBEntry._ID)
                );
                String wifiName = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.DBEntry.COLUMN_NAME_WIFI_NAME));
                String wifiLat = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.DBEntry.COLUMN_NAME_LAT));
                String wifiLong = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.DBEntry.COLUMN_NAME_LONG));

                Log.i("LocFinder", "Read db, id : " + itemId + " Wifi " + wifiName + " lat: " + wifiLat + " long:" + wifiLong);
            } while (cursor.moveToNext());
        }

        cursor.close();
    }
}
