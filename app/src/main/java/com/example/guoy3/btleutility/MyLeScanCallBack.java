package com.example.guoy3.btleutility;

import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanCallback;

import java.util.List;

/**
 * Created by guoy3 on 4/8/2015.
 */
public class MyLeScanCallBack extends ScanCallback {

    private static String TAG_NAME = MainActivity.TAG_NAME;
    private BTUIInterface m_UIInterface;

    public MyLeScanCallBack( BTUIInterface UIInterface )
    {
        m_UIInterface = UIInterface;
    }

    private MyLeScanCallBack(){ throw new UnsupportedOperationException();}

    @Override
    public void onScanResult (int callbackType, ScanResult result)
    {
        //m_UIInterface.pushMessage("Enter onScanResult()", false);

        if( android.bluetooth.le.ScanSettings.CALLBACK_TYPE_ALL_MATCHES == callbackType )
        {
            m_UIInterface.addToTableView( result.getDevice(), result.getRssi(),  result.getScanRecord() );
        }
        else
        {
            throw new AssertionError( "Undefined behavior in current SDK.");
        }
    }

    @Override
    public void onScanFailed (int errorCode)
    {
        m_UIInterface.pushMessage("Enter OnScanFailed()" + errorCode, true);
    }

    public void onBatchScanResults (List<ScanResult> results)
    {
        m_UIInterface.pushMessage("Enter onBatchScanResults()", false);
        throw new AssertionError( "TBD - need set ScanSettings argument");
    }
    /*
    public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
        m_UIInterface.LogText("found one BTLE device.", MainActivity.LOG_TYPE.info);

        if( null != m_UIInterface ){
            m_UIInterface.AddToTableView( bluetoothDevice );
        }
    }*/
}
