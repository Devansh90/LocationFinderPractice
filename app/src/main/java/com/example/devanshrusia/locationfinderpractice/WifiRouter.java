package com.example.devanshrusia.locationfinderpractice;

/**
 * Created by devanshrusia on 6/8/16.
 */
public class WifiRouter implements Comparable<WifiRouter> {

    private String bssid;
    private int strength;

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getStrength() {

        return strength;
    }

    public String getBssid() {

        return bssid;
    }

    public WifiRouter(String bssid, int strength) {
        this.bssid = bssid;
        this.strength = strength;
    }

    //Descending sort
    @Override
    public int compareTo(WifiRouter wifiRouter) {
        if (this.getStrength() < wifiRouter.getStrength())
            return this.getStrength();
        return wifiRouter.getStrength();
    }
}
