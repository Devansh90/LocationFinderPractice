package com.example.devanshrusia.locationfinderpractice;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WifiIntentService extends IntentService {

    public WifiIntentService() {
        super("WifiIntentService");
    }

    private WifiManager wifiManager;

    public static volatile boolean shouldContinue = true;

    private final BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                List<ScanResult> mScanResults = wifiManager.getScanResults();
                // add your logic here
//                Log.i("LocFinder", "Intent Inside scan");
//                Log.i("LocFinder", "Intent Scan:" + mScanResults.toString());

                setupTable(mScanResults);
            }
        }
    };

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("LocFinder", "Starting Handle intent");
        while (true) {
            if (!shouldContinue) {
                Log.i("LocFinder", " Turn off service");
                return;
            }
//            Log.i("LocFinder", " Inside Handle intent");
            fetchWifiData();
            SystemClock.sleep(5000);
        }
    }

    private void fetchWifiData() {
//        showAlertDialog("Refreshing Wifi");
//        Log.i("LocFinder","Starting intent wifi fetch");
//        String ssid;
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
//        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

//        if (mWifi.isConnected()) {
//            Log.i("LocFinder", "Connection present");
//        }
//        if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.getSSID())) {
//            ssid = connectionInfo.getSSID();
////            Log.i("LocFinder", "Connection info : " + connectionInfo.toString());
////            Log.i("LocFinder", "Connection SSID : " + ssid);
//        }
//        Log.i("LocFinder", "Wifi current state: " + wifiManager.getWifiState());
//        Log.i("LocFinder", "Previous Wifi current state: " + wifiManager.getWifiState());

        registerReceiver(mWifiScanReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
//        Log.i("LocFinder","Wifi data fetched");
    }

    private void setupTable(List<ScanResult> mScanResults) {

        Map<String, ArrayList<WifiRouter>> wifiList = new HashMap<>();

        for (ScanResult scanResult : mScanResults) {

            WifiRouter wifiRouter = new WifiRouter(scanResult.BSSID, scanResult.level);

            ArrayList<WifiRouter> wifiObject = wifiList.get(scanResult.SSID);

//                //Create new Array if empty
            if (wifiObject == null || wifiObject.isEmpty())
                wifiObject = new ArrayList<>();
            wifiObject.add(wifiRouter);
            wifiList.put(scanResult.SSID, wifiObject);

//            newString.append(scanResult.SSID + " BSSID :" + scanResult.BSSID.substring(0, 4) + " Level : " + scanResult.level + " \n");
//            Log.i("LocFinder", "Intent Done check for :" + scanResult.SSID);

        }
//        hideAlertDialog();
        EventBus.getDefault().post(new WifiEvent(wifiList, mScanResults.size()));
    }

    public void fetchMerchant(String wifiSSID, DBContract.DBReaderHelper mDbHelper) {
        Log.i("LocFinder","Looking for wifi Data");
        wifiSSID = wifiSSID.toLowerCase();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + DBContract.DBEntry.TABLE_NAME + " where " + DBContract.DBEntry.COLUMN_NAME_WIFI_NAME + " = " + wifiSSID;

        Cursor cursor = db.rawQuery(selectQuery, null);

        Log.i("LocFinder","DB query run complete");
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            Log.i("LocFinder","Found DB data");
            long itemId = cursor.getLong(
                    cursor.getColumnIndexOrThrow(DBContract.DBEntry._ID)
            );
            String wifiName = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.DBEntry.COLUMN_NAME_WIFI_NAME));
            String wifiLat = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.DBEntry.COLUMN_NAME_LAT));
            String wifiLong = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.DBEntry.COLUMN_NAME_LONG));

            Log.i("LocFinder", "Read db, id : " + itemId + " Wifi " + wifiName + " lat: " + wifiLat + " long:" + wifiLong);
        }
        cursor.close();
    }
}
