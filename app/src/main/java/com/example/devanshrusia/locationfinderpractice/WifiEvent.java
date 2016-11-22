package com.example.devanshrusia.locationfinderpractice;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by devanshrusia on 6/9/16.
 */
public class WifiEvent {
    Map<String, ArrayList<WifiRouter>> wifiList;
    int size;

    public int getSize() {
        return size;
    }

    public Map<String, ArrayList<WifiRouter>> getWifiList() {
        return wifiList;
    }

    public WifiEvent(Map<String, ArrayList<WifiRouter>> wifiList, int size) {

        this.wifiList = wifiList;
        this.size = size;
    }
}
