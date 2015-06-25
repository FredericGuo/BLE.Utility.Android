package com.example.guoy3.btleutility;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanRecord;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.guoy3.btleutility.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class BTDetailActivity extends ActionBarActivity implements BTUIInterface {

    private final String INTENT_DETAIL_MESSAGE = "com.example.guoy3.BTLEUTILITY.DETAIL";
    BroadcastReceiver resultReceiver;
    private final String TAG_NAME = "BTLE_DETAIL";

    private ArrayAdapter<DetailListViewData> m_DetailListAdapter;
    private ArrayList<DetailListViewData> m_DetailInfoList;

    public final static String MAC_ADDRESS = "MAC_ADDRESS";
    public final static String BLE_NAME = "BLE_NAME";
    //public final static String DEVICE_UUIDS = "DEVICE_UUIDS";
    private String m_macAddress;

    private int m_logCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        m_DetailInfoList = new ArrayList<DetailListViewData>();
        m_DetailListAdapter = new DetailArrayAdapter(this, m_DetailInfoList);

        m_logCount = 0;

        BTDeviceMgr deviceMgr = BTDeviceMgr.createBTDeviceManager(this);
        deviceMgr.setUIHost(this);

        // Show the Up button in the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        resultReceiver = createBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                resultReceiver,
                new IntentFilter(INTENT_DETAIL_MESSAGE));

        Intent intent = getIntent();
        String macAddress = intent.getStringExtra(MAC_ADDRESS);
        String bleName = intent.getStringExtra(BLE_NAME);
        if( null == bleName || 0 == bleName.length() ) {
            bleName = "Unknown";
        }
        setTitle(bleName);
        m_macAddress = macAddress; //for further access / discovery

        /*
        DetailListViewData data = new DetailListViewData();
        data.textForDisplay = "mac : ";
        if (macAddress != null && 0 < macAddress.length() ) {
            data.textForDisplay += macAddress;
        }
        m_DetailInfoList.add( data );
        */
        /*
        data = new DetailListViewData();
        data.textForDisplay =  "ble name : ";
        if (bleName != null && 0 < bleName.length() ) {
            data.textForDisplay +=  bleName;
        }
        m_DetailInfoList.add( data );
        */
        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        /*
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(BTDetailFragment.BLE_NAME,
                    getIntent().getStringExtra(BTDetailFragment.BLE_NAME));
            arguments.putString(BTDetailFragment.MAC_ADDRESS,
                    getIntent().getStringExtra(BTDetailFragment.MAC_ADDRESS));
            arguments.putString(BTDetailFragment.DEVICE_UUIDS,
                    getIntent().getStringExtra(BTDetailFragment.DEVICE_UUIDS));

            BTDetailFragment fragment = new BTDetailFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .add(R.id.item_detail_container, fragment)
                    .commit();
        }*/
    }


    @Override
    protected void onStart() {
        super.onStart();

        ListView detailListView = (ListView) findViewById(R.id.BTDetailListView);
        detailListView.setAdapter(m_DetailListAdapter);
        //m_DetailListAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch( item.getItemId() ) {
            case android.R.id.home: {
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                //navigateUpTo(new Intent(this, MainActivity.class));
                //NavUtils.navigateUpFromSameTask(this);
                finish();
                return true;
            }
            case R.id.ble_connect: {

                LogText("Start to connect device.", false);
                BTDeviceMgr deviceMgr = BTDeviceMgr.createBTDeviceManager(this);
                BluetoothManager btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
                BluetoothAdapter btAdapter = btManager.getAdapter();
                deviceMgr.talkWithDevice(this,btAdapter,m_macAddress);

                return true;
            }
            case R.id.ble_disconnect: {
                LogText("Start to disconnect device.", false );
                BTDeviceMgr deviceMgr = BTDeviceMgr.createBTDeviceManager(this);
                BluetoothManager btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
                BluetoothAdapter btAdapter = btManager.getAdapter();
                deviceMgr.disconnectDevice();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void addToTableView( BluetoothDevice device, int RSSI, ScanRecord record )
    {
        pushMessage("should not call BTDetailActivity::AddToTableView.", true);
    }
    public void pushMessage( String message, boolean isError )
    {
        Intent intent = new Intent(INTENT_DETAIL_MESSAGE);
        intent.putExtra("message", message);
        intent.putExtra("error", isError );
        LocalBroadcastManager.getInstance(BTDetailActivity.this)
                .sendBroadcast(intent);
    }

    public void addToDetailView(List<String> characteristicUUIDs){
        //TextView logView = (TextView) findViewById( R.id.BTLELogWindow );

        DetailListViewData data = new DetailListViewData();
        data.textForDisplay =  "  Characteristic Attributes:";
        m_DetailInfoList.add(data);

        for( String aUUID : characteristicUUIDs) {
            data = new DetailListViewData();
            data.textForDisplay =  "  " + aUUID;
           // if( -1==m_DetailInfoList.indexOf(data.textForDisplay)) {
                m_DetailInfoList.add(data);
            //}
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_DetailListAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_btdetail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private BroadcastReceiver createBroadcastReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LogText(intent.getStringExtra("message"),intent.getBooleanExtra("error", false) );
            }
        };
    }

    private void LogText(String message, boolean bError)
    {
        if( bError ){
            Log.e(TAG_NAME, message);
        }else{
            Log.i(TAG_NAME, message);
        }

        TextView logView = (TextView) findViewById( R.id.BTDetailLogWindow );
        CharSequence cs = m_logCount++ + " " + message + "\r\n";
        logView.append( cs );
        if( MainActivity.MAX_LOG_LINE < logView.getLineCount())
        {
            CharSequence chars = logView.getText();
            String logText = chars.toString();
            int index = logText.indexOf('\n');
            index = logText.indexOf('\n', index+1);
            String nextText = logText.substring(index);
            logView.setText( nextText );
        }

        ScrollView logScrollView = (ScrollView) findViewById( R.id.detailLogScrollView );
        logScrollView.fullScroll(View.FOCUS_DOWN);
    }
}
