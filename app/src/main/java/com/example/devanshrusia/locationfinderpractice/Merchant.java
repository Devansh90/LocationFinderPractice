package com.example.devanshrusia.locationfinderpractice;

import java.util.ArrayList;

/**
 * Created by devanshrusia on 6/8/16.
 */
public class Merchant implements Comparable<Merchant> {

    public String getSSID() {
        return SSID;
    }

    public ArrayList<WifiRouter> getWifiRouterArrayList() {
        return wifiRouterArrayList;
    }

    public int getStrength() {
        return strength;
    }

    public int getMetricScore() {
        return metricScore;
    }

    private String SSID;
    private ArrayList<WifiRouter> wifiRouterArrayList;
    private int strength;
    private int metricScore;

    public Merchant(String SSID, ArrayList<WifiRouter> wifiRouterArrayList, int strength, int metricScore) {
        this.SSID = SSID;
        this.wifiRouterArrayList = wifiRouterArrayList;
        this.strength = strength;
        this.metricScore = metricScore;
    }

    //Descending list
    @Override
    public int compareTo(Merchant merchant) {
//        Log.i("LocFinder", "Comparing " + this.SSID + " with " + merchant.SSID);
//        Log.i("LocFinder", "Comparing " + this.strength + " with " + merchant.strength);
//
//        Log.i("LocFinder", "Returning " + (merchant.getStrength() - this.strength));
        return merchant.metricScore - this.metricScore;
    }
}
