package com.example.guoy3.btleutility;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanRecord;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guoy3 on 4/8/2015.
 */
class BTDeviceMgr extends BluetoothGattCallback {

    private static volatile BTDeviceMgr m_instance = null;

    /*class BTNode
    {
        BluetoothDevice device;
        BluetoothGatt gatt; //gatt server
        ScanRecord record;
    };*/

    public class BLE_INFO{
        public String macAddress;
        public String name;
        public String deviceUUIDs;
    }

    enum BT_ACTION
    {
        BT_CONNECTING ,
        BT_CONNECTED ,
        BT_DISCONNECTED
    }

    public static String TAG_NAME = MainActivity.TAG_NAME;
    private BTUIInterface m_UIInterface;
    private ArrayList<BLE_INFO> m_btDevicesList;

    private BT_ACTION m_btActionStatus;
    private BluetoothGatt m_currentGatt;
    private String m_currentDeviceMac;
    private Context m_currentContext;
    private BluetoothDevice m_currentDevice;
    private BluetoothDevice m_SavedDevice;

    private void ResetCurrentDevice()
    {
        m_currentDeviceMac = "";
        m_currentContext = null;
        m_currentDevice = null;
        if( null != m_currentGatt )
        {
            m_currentGatt.close();
            m_currentGatt = null;
        }
    }

    public void clearDevices(){
        m_btDevicesList.clear();
        m_btActionStatus = BT_ACTION.BT_DISCONNECTED;
        ResetCurrentDevice();
    }

    public BLE_INFO getDeviceInfo( final int index ){
        //BLE_INFO info = new BLE_INFO();

        BLE_INFO info =  m_btDevicesList.get(index);
        //BluetoothDevice btDevice = m_btDevicesList.get(index).device;
        //info.macAddress = btDevice.getAddress();
        //info.name = btDevice.getName();

        /*
        ScanRecord record = m_btDevicesList.get(index).record;
        List<ParcelUuid> uuids = record.getServiceUuids();
        if( null != uuids ) {
            info.deviceUUIDs = "";
            for(int i = 0; i< uuids.size(); ++i )
            {
                info.deviceUUIDs += "\r\n";
                info.deviceUUIDs += uuids.get(i).toString();
            }
        }*/

        return info;
    }

    public boolean talkWithDevice( Context context, BluetoothAdapter btAdapter, String macAddress  ){
        m_UIInterface.pushMessage("Enter talkWithDevice()", false);


        if (m_currentDeviceMac != null && macAddress.equals(m_currentDeviceMac)
                && m_currentGatt != null) {
            Log.d(TAG_NAME, "Trying to use an existing mBluetoothGatt for connection.");
            if (m_currentGatt.connect()) {
                m_btActionStatus = BT_ACTION.BT_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        boolean bFoundDevice = false;
        for( BLE_INFO node : m_btDevicesList)
        {
            if( 0==  node.macAddress.compareToIgnoreCase(macAddress)){
                m_UIInterface.pushMessage("Found the mac address in list", false);
                bFoundDevice = true;
                break;
            }
        }
        if(!bFoundDevice ){
            m_UIInterface.pushMessage("NOT find the mac address in list", false);
        }

        //final BluetoothDevice device = m_SavedDevice;//btAdapter.getRemoteDevice(macAddress);
        final BluetoothDevice device = btAdapter.getRemoteDevice(macAddress);
        if (device == null) {
            m_UIInterface.pushMessage( "getRemoteDevice failed.", true);
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        m_currentGatt = device.connectGatt(context, false, this);

        //BluetoothGatt bluetoothGatt =  btDevice.connectGatt(context, false, this);
        if( null == m_currentGatt ){
            m_UIInterface.pushMessage( "Error : connectGatt()", true);
            return false;
        }
        m_currentDeviceMac = macAddress;
        m_btActionStatus = BT_ACTION.BT_CONNECTING;
        m_currentContext = context;
        m_currentDevice = device;

        //m_btDevicesList.get(index).gatt = m_btGatt;
        m_UIInterface.pushMessage( "Server Gatt hash: " + m_currentGatt.hashCode() , false);
        return true;
    }

    public static BTDeviceMgr createBTDeviceManager( BTUIInterface btUIInterface )
    {
        if( null == m_instance )
        {
           synchronized (BTDeviceMgr.class) {
               m_instance = new BTDeviceMgr(btUIInterface);
           }
        }

        return  m_instance;
    }

    void setUIHost( BTUIInterface btUIInterfac ){
        m_UIInterface = btUIInterfac;
    }

    private BTDeviceMgr( BTUIInterface btUIInterface ){
        m_UIInterface = btUIInterface;
        m_btDevicesList = new ArrayList<BLE_INFO>();
        m_btActionStatus = BT_ACTION.BT_DISCONNECTED;
        ResetCurrentDevice();
    }

    public void addDevice( BluetoothDevice btDevice, ScanRecord record){
        BLE_INFO node = new BLE_INFO();
        node.name = btDevice.getName();
        node.macAddress = btDevice.getAddress();

        String devName =  btDevice.getName();
        if(null != devName && btDevice.getName().equals("MI")) {
            m_SavedDevice = btDevice;
        }

        List<ParcelUuid> uuids = record.getServiceUuids();
        if( null != uuids ) {
            node.deviceUUIDs = "";
            for (int i = 0; i < uuids.size(); ++i) {
                node.deviceUUIDs += "\r\n";
                node.deviceUUIDs += uuids.get(i).toString();
            }
        }

        m_btDevicesList.add(node);
    }

    public void disconnectDevice()
    {
        if( null != m_currentGatt )
        {
            m_currentGatt.disconnect();
        }
        ResetCurrentDevice();
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
        // this will get called anytime you perform a read or write characteristic operation
    }

    @Override
    public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
        m_UIInterface.pushMessage( "Enter GattCallback::onConnectionStateChange()", false);
        m_UIInterface.pushMessage( "state: " + status + ", newState: " + newState , false);
        m_UIInterface.pushMessage("GATT client: " + gatt.hashCode(), false);
        m_UIInterface.pushMessage("GATT (old) client: " + m_currentGatt.hashCode(), false);

        if (newState == BluetoothProfile.STATE_CONNECTED) {
            //intentAction = ACTION_GATT_CONNECTED;
            //mConnectionState = STATE_CONNECTED;
            //broadcastUpdate(intentAction);
            m_UIInterface.pushMessage("Connected to GATT server.", false);
            if( !m_currentGatt.discoverServices()){
                m_UIInterface.pushMessage("Failed to discover BT service.", true);
            }
            m_btActionStatus = BT_ACTION.BT_CONNECTED;
            m_UIInterface.pushMessage("Start to BT discover service.", false );

        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            m_UIInterface.pushMessage("Disconnected to Gatt server.", false);

            if( m_btActionStatus == BT_ACTION.BT_CONNECTING )
            {
                Log.i(TAG_NAME, "retry connection - BT_CONNECTING");
                //try again
                if(null != m_currentGatt) {
                    m_currentGatt.connect();
                    Log.i(TAG_NAME, "started retry connection");
                }
                /*
                if(0< m_currentDeviceMac.length()&& null != m_currentDevice) {
                    m_currentGatt = null;
                    m_currentGatt = m_currentDevice.connectGatt(m_currentContext, false, this);
                    m_UIInterface.pushMessage("started retry connection.", false);
                    Log.i(TAG_NAME, "Started - retry connection.");
                }
                else
                {
                    m_btActionStatus = BT_ACTION.BT_DISCONNECTED;
                    m_UIInterface.pushMessage("can not retry connection.", true);
                }*/
            }
            else
            {
                Log.i(TAG_NAME, "can NOT retry connection : " + m_btActionStatus.toString());
                if (null != m_currentGatt) {
                    m_currentGatt.close();
                    m_currentGatt = null;
                    m_currentContext = null;
                }
                gatt.close();
            }
            //intentAction = ACTION_GATT_DISCONNECTED;
            //mConnectionState = STATE_DISCONNECTED;
            //Log.i(TAG, "Disconnected from GATT server.");
            //broadcastUpdate(intentAction);
        }
    }

    @Override
    public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
        m_UIInterface.pushMessage( "Enter GattCallback::onServicesDiscovered()", false);
        //m_UIInterface.pushMessage( "state: " + status , false);
        //m_UIInterface.pushMessage( "GATT client: " + gatt.hashCode(), false);

        if (status == BluetoothGatt.GATT_SUCCESS) {
            m_UIInterface.pushMessage( "Discovered GATT client: " + gatt.hashCode(), false);
            //m_UIInterface.pushMessage("service discovered succssfully.", false);

            List<BluetoothGattService> services = gatt.getServices();
            List<String> characterUUIDs = new ArrayList<String>();

            for( BluetoothGattService service:services){
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                for( BluetoothGattCharacteristic character : characteristics) {

                    StringBuilder builder = new StringBuilder();
                    builder.append( character.getUuid().toString());
                    builder.append( " ==> ");
                    int property = character.getProperties();
                    if( 0 != ( property &  BluetoothGattCharacteristic.PROPERTY_READ)) {
                        builder.append( " Readable");
                    }
                    if( 0 != ( property &  BluetoothGattCharacteristic.PROPERTY_WRITE)) {
                        builder.append( " Writable");
                    }
                    if( 0 != ( property &  BluetoothGattCharacteristic.PROPERTY_NOTIFY)) {
                        builder.append( " Notify");
                    }
                    if( 0 != ( property &  BluetoothGattCharacteristic.PROPERTY_INDICATE)) {
                        builder.append( " Indicate");
                    }

                    characterUUIDs.add(builder.toString());
                }
            }

            m_UIInterface.addToDetailView(characterUUIDs);

        } else {
            m_UIInterface.pushMessage("service discovery failed..", false);
        }
    }


}
