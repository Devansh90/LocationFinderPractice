package com.example.devanshrusia.locationfinderpractice;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WifiActivity extends AppCompatActivity {

    public double cellLat;
    public double cellLng;
    public double cellAccuracy;

    private TextView largeTextView;
    private WifiManager wifiManager;
    private AlertDialog mAlertDialog;

    private Intent msgIntent;
    private MerchantLocation merchantLoc;

    private final BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                List<ScanResult> mScanResults = wifiManager.getScanResults();
//                Log.i("LocFinder", "Inside scan");
//                Log.i("LocFinder", "Scan:" + mScanResults.toString());

                setupTable(mScanResults);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);

        merchantLoc = new MerchantLocation();
        merchantLoc.init(getApplicationContext());
        merchantLoc.setupDb();

        Log.i("LocFinder", "Db setup complete");
        msgIntent = new Intent(this, WifiIntentService.class);
        fetchCellData();

        _getLocation();
        startIntentService();
    }

    private void setupTable(List<ScanResult> mScanResults) {

        int current = 1;
//        StringBuilder newString = new StringBuilder("");
        LinearLayout scrollView = (LinearLayout) findViewById(R.id.wifiScrollView);
        scrollView.removeAllViews();

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
//            Log.i("LocFinder", "Done check for :" + scanResult.SSID);

        }
        largeTextView.setText("Found " + mScanResults.size() + " wifi access point\nYou are most likly at the topmost point \n");
        largeTextView.setTextSize(14);

        ArrayList<Merchant> merchantList = new ArrayList<>();
        for (Map.Entry<String, ArrayList<WifiRouter>> entry : wifiList.entrySet()) {
            String key = entry.getKey();
            ArrayList<WifiRouter> value = entry.getValue();
            int strength = fetchStrength(value);
            merchantList.add(new Merchant(key, value, strength, getMetricScore(value.size(), strength)));
        }
        // Sorting MerchantList
        Collections.sort(merchantList);

        for (Merchant merchant : merchantList) {
            TextView labelTV = new TextView(this);
            labelTV.setId(200 + current);
            labelTV.setText(merchant.getSSID() + " with " + merchant.getWifiRouterArrayList().size() + " routers " + " Score:" + merchant.getMetricScore() + "\n");
            labelTV.setTextColor(Color.BLACK);
            labelTV.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.FILL_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));
            labelTV.setTextSize(12);
            scrollView.addView(labelTV);
        }
    }

    private int getMetricScore(int size, int strength) {
        int routerWeight = 25;
        int strengthWeight = 1;
        return (size * routerWeight) + (strength * strengthWeight);
    }

    private int fetchStrength(ArrayList<WifiRouter> value) {
        if (value.size() < 2)
            return value.get(0).getStrength();
        return (value.get(0).getStrength() + value.get(1).getStrength()) / 2;
    }

    private void startIntentService() {

//        Log.i("LocFinder", "Starting service");
        WifiIntentService.shouldContinue = true;
        startService(msgIntent);
    }

    private void fetchCellData() {
        if (getIntent() != null) {
            Log.i("LocFinder", "Intent not null");
            Intent intent = getIntent();
            cellLat = intent.getDoubleExtra(MapsActivity.latConstant, 0.0000);
            cellLng = intent.getDoubleExtra(MapsActivity.longConstant, 0.0000);
            cellAccuracy = intent.getDoubleExtra(MapsActivity.accuracyConstant, 0);
            Log.i("LocFinder", "Lat : " + cellLat + " long : " + cellLng + " accuracy: " + cellAccuracy);
        }

        largeTextView = (TextView) findViewById(R.id.wifiLargeText);
//        StringBuilder largeText = new StringBuilder("Cell Lat: " + cellLat + " \nCell Lng: " + cellLng);
//        largeTextView.setText(largeText);
//        largeTextView.setVisibility(View.INVISIBLE);
        fetchWifiData();
    }

    private void fetchWifiData() {
        String ssid;
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
//        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

//        if (mWifi.isConnected()) {
//            Log.i("LocFinder", "Connection present");
//        }
        if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.getSSID())) {
            ssid = connectionInfo.getSSID();
            Log.i("LocFinder", "Connection info : " + connectionInfo.toString());
            Log.i("LocFinder", "Connection SSID : " + ssid);
        }
        Log.i("LocFinder", "Wifi current state: " + wifiManager.getWifiState());
        Log.i("LocFinder", "Previous Wifi current state: " + wifiManager.getWifiState());

        registerReceiver(mWifiScanReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
    }

    public void workWithWifi(View view) {
        Log.i("LocFinder", "Wifi button pressed, stopping service...");
//        fetchWifiData();

//        msgIntent = new Intent(this, WifiIntentService.class);
//        msgIntent.putExtra("stoptask","stop");
//        stopService(msgIntent);

        WifiIntentService.shouldContinue = false;
    }

    @Subscribe
    public void onMessageEvent(WifiEvent event) {
//        Log.i("LocFinder", "Recieved correct event, processing.......");
        displayEvent(event);
    }

    // This method will be called when a SomeOtherEvent is posted
    @Subscribe
    public void handleSomethingElse(WifiEvent event) {
//        Log.i("LocFinder", "Don`t know what to do next?");
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        Log.i("LocFinder", "Registering for event");
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
        Log.i("LocFinder", "Events registry complete");
    }

    private void displayEvent(WifiEvent wifiEvent) {
        hideAlertDialog();
//        Log.i("LocFinder","Items to display");
        int current = 1;
//        StringBuilder newString = new StringBuilder("");
        LinearLayout scrollView = (LinearLayout) findViewById(R.id.wifiScrollView);
        scrollView.removeAllViews();

        largeTextView = (TextView) findViewById(R.id.wifiLargeText);
        largeTextView.setText("Found " + wifiEvent.getSize() + " wifi access point\nYou are most likly at the topmost point \n");
        largeTextView.setTextSize(12);

        ArrayList<Merchant> merchantList = new ArrayList<>();
        for (Map.Entry<String, ArrayList<WifiRouter>> entry : wifiEvent.getWifiList().entrySet()) {
            String key = entry.getKey();
            ArrayList<WifiRouter> value = entry.getValue();
            int strength = fetchStrength(value);
            merchantList.add(new Merchant(key, value, strength, getMetricScore(value.size(), strength)));
//            Log.i("LocFinder","Added:" + key + " stregth:" + strength);
        }
        // Sorting MerchantList
        Collections.sort(merchantList);

        for (Merchant merchant : merchantList) {
            if (merchant.getWifiRouterArrayList().size() < 20) {
                TextView labelTV = new TextView(this);
                labelTV.setId(200 + current);
                labelTV.setText(merchant.getSSID() + " with " + merchant.getWifiRouterArrayList().size() + " routers " + " Score:" + merchant.getMetricScore() + "\n");
                labelTV.setTextColor(Color.BLACK);
                labelTV.setLayoutParams(new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.FILL_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT));
                labelTV.setTextSize(12);
                if (current == 1) {
                    labelTV.setTextColor(Color.RED);
                    labelTV.setTypeface(null, Typeface.BOLD);

                    if (showPopUpForMerchant(merchant.getSSID(), merchant.getMetricScore())) {

                        merchantLoc.fetchMerchant(merchant.getSSID());
                        showAlertDialog(merchant.getSSID());
                    }

                }
                current++;
//                Log.i("LocFinder","Adding :" + labelTV.getText());
                scrollView.addView(labelTV);
            }
        }

        Log.i("LocFinder", "Refresh complete");
    }

    private boolean showPopUpForMerchant(String ssid, int metricScore) {
        if (ssid.contains("OLA"))
            if (metricScore > 50)
                return true;
        if (ssid.equals("janu") || ssid.contains("Titan") || ssid.contains("Tani") || ssid.contains("TITAN"))
            if (metricScore > -26)
                return true;
        return false;
    }

    private void showAlertDialog(String message) {
        triggerNotification(message);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Welcome to..");
        builder.setMessage(message);
        mAlertDialog = builder.create();
        mAlertDialog.show();
    }

    private void hideAlertDialog() {
        if (mAlertDialog != null) {
//            Log.i("LocFinder", "Hiding alert");
            mAlertDialog.cancel();
            mAlertDialog.dismiss();
        }
    }

    private void _getLocation() {
        Map<String, Double> coarseLocation = new HashMap<>();
        LocationManager lm = (LocationManager) getSystemService(
                Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);

        Location l = null;

        for (int i = providers.size() - 1; i >= 0; i--) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            l = lm.getLastKnownLocation(providers.get(i));
            if (l != null) break;
        }

        if (l != null) {
//            Log.i("LocFinder", "Found some data");
            coarseLocation.put("lat", l.getLatitude());
            coarseLocation.put("long", l.getLongitude());
            coarseLocation.put("accuracy", (double) l.getAccuracy());
        }

        Log.i("LocFinder", "CoarseLoc: Lat : " + coarseLocation.get("lat") + " long: " + coarseLocation.get("long") + " accuracy: " + coarseLocation.get("accuracy"));
        Log.i("LocFinder", "CellLat   : Lat : " + cellLat + " long: " + cellLng + " accuracy: " + cellAccuracy);

//        showAlertDialog("CoarseLoc: Lat : " + coarseLocation.get("lat") + " long: " + coarseLocation.get("long") + " accuracy: " + coarseLocation.get("accuracy") + "\n" + "CellLat   : Lat : " + cellLat + " long: " + cellLng + " accuracy: " + cellAccuracy);
    }

    public void triggerNotification(String message) {
//        Log.i("LocFinder", "Triggering Notify");

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        builder.setSound(alarmSound);

        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                        .setContentTitle("Welcome too...")
                        .setCategory(Notification.CATEGORY_MESSAGE)
                        .setContentText(message)
                        .setSound(alarmSound)
                        .setVibrate(new long[]{1000, 1000});
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, WifiActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(WifiActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        int mId = 1;
        mNotificationManager.notify(mId, mBuilder.build());
//        Log.i("LocFinder", "Notify done");
    }

}
