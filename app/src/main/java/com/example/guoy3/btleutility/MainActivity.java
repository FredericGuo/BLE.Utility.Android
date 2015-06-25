package com.example.guoy3.btleutility;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.example.guoy3.btleutility.BTDeviceAdapter.OnItemClickListener;

public class MainActivity extends ActionBarActivity implements BTUIInterface {

    public final static String TAG_NAME = "BTLE_Utility";
    public final static int MAX_LOG_LINE = 200;

    //private ArrayAdapter<DeviceListViewData> m_listAdapter;
    private BTDeviceAdapter m_listAdapter;
    private ArrayList<DeviceListViewData> m_btDeviceInfoList;
    private long m_logCount;

    private final String INTENT_UI_MESSAGE = "com.example.guoy3.BTLEUTILITY";

    private static int REQUEST_ENABLE_BT = 1000;

    private android.bluetooth.le.ScanCallback BTLEScanCallback;
    BluetoothAdapter btAdapter;
    BluetoothManager btManager;
    BTDeviceMgr deviceMgr;

    private int m_selectedListViewIndex;

    BroadcastReceiver resultReceiver;
    private Handler msgHandler;
    private static final long SCAN_PERIOD = 3600000;//60mins  120000; //120 sec

    enum BT_STATUS{
        BT_SCANNING,
        BT_IDLE, //either scanned or scanning is cancelled in half way.
        BT_UNKNOWN
    }
    BT_STATUS m_btStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resultReceiver = createBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                resultReceiver,
                new IntentFilter(INTENT_UI_MESSAGE));

        m_btDeviceInfoList = new ArrayList<DeviceListViewData>();

        m_listAdapter = new BTDeviceAdapter( m_btDeviceInfoList); //new BTArrayAdapter(this,m_btDeviceInfoList);
        //m_listAdapter = new ArrayAdapter<String>(this, R.layout.btdevicerow, m_btDeviceInfoList);
        msgHandler = new Handler();
        deviceMgr = BTDeviceMgr.createBTDeviceManager(this);

        m_btStatus = BT_STATUS.BT_IDLE; //NOTE: btStatus can not be in resetBTStatus(),
                                        //because it is called inside StartScanBTLE().
        m_logCount = 0;
        resetBTStatus();

        setupDeviceListView();
        InitialBLEScan();
    }

    private void resetBTStatus()
    {
        m_selectedListViewIndex = -1;

        //reset UI before each scan
        deviceMgr.clearDevices();
        m_btDeviceInfoList.clear();
        m_listAdapter.notifyDataSetChanged();
    }

    private void InitialBLEScan()
    {
        btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        BTLEScanCallback = new MyLeScanCallBack(this);
        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,REQUEST_ENABLE_BT);
        }
        else
        {
            //StartScanBTLE( btAdapter );
        }
    }

    private void StartScanBTLE(final BluetoothAdapter adapter)
    {
        LogText("Enter StartScanBTLE()", false);

        resetBTStatus();

        //Stop current scanning if exist
        msgHandler.removeCallbacksAndMessages(null);
        if( BT_STATUS.BT_SCANNING == m_btStatus ){
            StopScanBTLE( btAdapter );
        }

        msgHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (BT_STATUS.BT_SCANNING == m_btStatus) {
                    StopScanBTLE(adapter);
                }
            }
        }, SCAN_PERIOD);

        //btAdapter.getBluetoothLeScanner().startScan(BTLEScanCallback);
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build();
        btAdapter.getBluetoothLeScanner().startScan(null, settings, BTLEScanCallback);
        m_btStatus = BT_STATUS.BT_SCANNING;
        LogText("Start scanning BTLE device ...", false);
    }
    private void StopScanBTLE(BluetoothAdapter adapter)
    {
        LogText("Stop scanning BTLE device", false);
        adapter.getBluetoothLeScanner().stopScan(BTLEScanCallback);
        m_btStatus = BT_STATUS.BT_IDLE;
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                LogText("BT is enabled by user.", false);
                //StartScanBTLE(btAdapter);
            }
            else
            {
                LogText("BT has not been enabled.", true);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void OnRescan( View view)
    {
        LogText("Enter OnRescan", false);

        StartScanBTLE( btAdapter );
    }

    /*
    public void OnConnect( View view)
    {
        LogText("Enter OnConnect", false);

        if( 0 <= m_selectedListViewIndex )
        {
            if( !deviceMgr.talkWithDevice( getApplicationContext(),btAdapter, m_selectedListViewIndex))
            {
                LogText("Error : OnConnect()", true);
            }
        }
    }*/

    public void OnStopScan( View view)
    {
        LogText("Enter OnStopScan", false);

        if( null == btAdapter ){
            LogText("failed to stop scan", true);
            return;
        }
        StopScanBTLE(btAdapter);

        //if( !m_btDeviceInfoList.isEmpty()) {
        //    m_btDeviceInfoList.remove(0);
        //    m_listAdapter.notifyDataSetChanged();
        //}
    }

    @Override
    public void addToTableView(BluetoothDevice btDevice, int RSSI, ScanRecord record )
    {
        deviceMgr.addDevice(btDevice, record);

        DeviceListViewData data = new DeviceListViewData();
        data.text1 =  btDevice.getName();
        if( "null".equals(data.text1))
        {
            return;
        }

        String BTType = "";
        switch( btDevice.getType())
        {
            case BluetoothDevice.DEVICE_TYPE_CLASSIC:
            {
                BTType = " BT";
                break;
            }
            case BluetoothDevice.DEVICE_TYPE_LE:
            {
                BTType = " BTLE";
                break;
            }
            case BluetoothDevice.DEVICE_TYPE_DUAL:
            {
                BTType = " BT.DUAL";
                break;
            }
            default:
            {
                BTType = " BT.Unknown";
            }
        }

        data.text2 =  btDevice.getAddress();
        data.text1 += BTType;
        data.RSSIValue = RSSI;

        //LogText("Dump device UUID for " + btDevice.getName(), false);
        //LogText("device mac : " + btDevice.getAddress(), false);
        /*List<ParcelUuid> listUUIDs = record.getServiceUuids();
        if( null != listUUIDs ) {
            for (ParcelUuid aUUID : listUUIDs) {
                LogText("features UUID : " + aUUID.toString(), false);
            }
        }*/

        for(DeviceListViewData viewData : m_btDeviceInfoList)
        {
            if( viewData.text1.equals( data.text1) &&
                    viewData.text2.equals( data.text2) )
            {
                if( viewData.RSSIValue != RSSI)
                {
                    viewData.RSSIValue = RSSI;
                    //LogText( btDevice.getName() +  " ========= RSSI ====== " + RSSI, false);
                    m_listAdapter.notifyDataSetChanged();
                }

                return;
            }
        }

        LogText("device Name : " + btDevice.getName(), false);
        LogText("device mac : " + btDevice.getAddress(), false);
        LogText("device RSSI : " + RSSI, false);
        m_btDeviceInfoList.add(data);
        m_listAdapter.notifyDataSetChanged();
    }

    private void setupDeviceListView(){

        RecyclerView btRecyclerView = (RecyclerView) findViewById( R.id.BTLERecyclerView );

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        btRecyclerView.setLayoutManager(layoutManager);
        btRecyclerView.setAdapter(m_listAdapter);

        //btRecyclerView.addItemDecoration(new DividerItemDecoration(MainActivity.this, DividerItemDecoration.HORIZONTAL_LIST));

        btRecyclerView.addItemDecoration(new ItemDivider(MainActivity.this,
                R.drawable.devicetablecellborder));
        /*
        m_listAdapter.setOnItemClickListener(new BTDeviceAdapter.OnItemClickListener() {

            @Override
            public void onClick(View parent, int position) {
                //DeviceListViewData data = new DeviceListViewData();
                //data.text1 = "insert";
                //data.text2 = "insert 2";
                //m_listAdapter.insert(data, position);
            }
        });
        */
        //btListView.setAdapter(m_listAdapter);

        m_listAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            //public void onClick(AdapterView<?> adapterView, View row, int position, long index) {
            public void onClick(View row, int position) {
                LogText("Click position : " + position, false);
                m_selectedListViewIndex = (position == -1) ? -1 : (int) position;

                if (0 <= m_selectedListViewIndex) {
                    Intent detailIntent = new Intent(getApplicationContext(), com.example.guoy3.btleutility.BTDetailActivity.class);

                    //if( !deviceMgr.talkWithDevice( getApplicationContext(),btAdapter, m_selectedListViewIndex))

                    BTDeviceMgr.BLE_INFO info = deviceMgr.getDeviceInfo(m_selectedListViewIndex);
                    detailIntent.putExtra(BTDetailActivity.BLE_NAME, info.name);
                    detailIntent.putExtra(BTDetailActivity.MAC_ADDRESS, info.macAddress);
                    startActivity(detailIntent);
                    return;
                }
            }
        });
        /*
        btListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                TextView deviceRow = (TextView) view.findViewById(R.id.BTLEListViewRow);
                String text = deviceRow.getText().toString();
                LogText("Click " + text, LOG_TYPE.info);

                int rid = deviceRow.getId();
                LogText("Clicked ID = " + rid, LOG_TYPE.info);
                LogText("View argu ID = " + view.getId(), LOG_TYPE.info);

                LogText("All rows :", LOG_TYPE.info);
                int count = parent.getCount();

                for( int i = 0; i < count; ++i )
                {
                    TextView aRow = (TextView)parent.getChildAt(i);
                    if( null != aRow ){
                        LogText("rID = " + aRow.getId(), LOG_TYPE.info);
                    }
                }
            }
        });*/
    }

    private BroadcastReceiver createBroadcastReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LogText(intent.getStringExtra("message"),intent.getBooleanExtra("error", false) );
            }
        };
    }

    public void pushMessage( String message, boolean isError  )
    {
        Intent intent = new Intent(INTENT_UI_MESSAGE);
        intent.putExtra("message", message);
        intent.putExtra("error", isError );
        LocalBroadcastManager.getInstance(MainActivity.this)
                .sendBroadcast(intent);
    }

    private void LogText(String message, boolean bError)
    {
        if( bError ){
            Log.e(TAG_NAME, message);
        }else{
            Log.i(TAG_NAME, message);
        }

        TextView logView = (TextView) findViewById( R.id.BTLELogWindow );
        CharSequence cs = m_logCount++ + " " + message + "\r\n";
        logView.append(cs);
        if( MainActivity.MAX_LOG_LINE < logView.getLineCount())
        {
            CharSequence chars = logView.getText();
            String logText = chars.toString();
            int index = logText.indexOf('\n');
            index = logText.indexOf('\n', index+1);
            String nextText = logText.substring(index);
            logView.setText( nextText );
        }

        ScrollView logScrollView = (ScrollView) findViewById( R.id.logScrollView );
        logScrollView.fullScroll(View.FOCUS_DOWN);
    }

    public void addToDetailView(List<String> serviceUUIDs){
        pushMessage("should not call MainActivity::addToDetailView.", true);
    }
}
