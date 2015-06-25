package com.example.guoy3.btleutility;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanRecord;

import java.util.List;

/**
 * Created by guoy3 on 4/8/2015.
 */
public interface BTUIInterface {
    public void addToTableView( BluetoothDevice device, int RSSI, ScanRecord record );
    public void pushMessage( String message, boolean isError );
    public void addToDetailView(List<String> characteristicUUIDs);
}
