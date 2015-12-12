package ch.ethz.inf.vs.fuckthisproject;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
    implements WifiP2pManager.ConnectionInfoListener
{

    private ToggleButton serviceButton;
    private Button startDiscoverButton;
    private Button stopDiscoverButton;
    private ToggleButton extraPeerDiscoverButton;
    private ToggleButton mappingTextButton;
    private Button clearButton;
    private ListView listView;

    private final IntentFilter intentFilter = new IntentFilter();

    private WiFiDirectBroadcastReceiver broadcastReceiver;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private WifiP2pDnsSdServiceRequest serviceRequest;

    public static final String TXTRECORD_PROP_AVAILABLE = "available";
    public static final String SERVICE_INSTANCE = "_safeMyAss";
    public static final String SERVICE_REG_TYPE = "_presence._tcp";

    protected WiFiDeviceListAdapter adapter;
    protected List<WifiP2pDevice> deviceList;

    private WifiP2pDnsSdServiceInfo service;

    public static String TAG = "## MainActivity ##";

    private String TestMapping = "Testbala";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serviceButton = (ToggleButton) findViewById( R.id.hostServiceButton );
        startDiscoverButton = (Button) findViewById( R.id.startDiscoverButton );
        stopDiscoverButton = (Button) findViewById( R.id.stopDiscoverButton );
        extraPeerDiscoverButton = (ToggleButton) findViewById( R.id.extraPeerDiscoverButton );
        clearButton = (Button) findViewById( R.id.clearButton );
        mappingTextButton = (ToggleButton) findViewById( R.id.mappingTextButton );
        listView = (ListView) findViewById( R.id.listView );

        deviceList = new ArrayList<>();
        adapter = new WiFiDeviceListAdapter( deviceList, MainActivity.this.getLayoutInflater() );
        listView.setAdapter( adapter );

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        serviceButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    startThisService();
                } else {
                    // The toggle is disabled
                    stopThisService();
                }
            }
        });

        startDiscoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDiscovery();
            }
        });

        stopDiscoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopDiscovery();
            }
        });

        extraPeerDiscoverButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    startExtraPeerDiscovery();
                } else {
                    // The toggle is disabled
                    stopExtraPeerDiscovery();
                }
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceList.clear();
                adapter.notifyDataSetChanged();
                manager.clearLocalServices(channel, createWifiManagerListener("333333333333333333", "000"));
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                connectWithPeer(deviceList.get(position));
            }
        });

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        broadcastReceiver = new WiFiDirectBroadcastReceiver( manager, channel, this );
        registerReceiver( broadcastReceiver, intentFilter );

    }

    public void onDestroy(){
        unregisterReceiver( broadcastReceiver );
        super.onDestroy();
    }

    private void startExtraPeerDiscovery() {
        manager.discoverPeers(channel, createWifiManagerListener("111", "000"));
    }

    private void stopExtraPeerDiscovery() {
        manager.stopPeerDiscovery(channel, createWifiManagerListener("222", "000"));
    }

    private void startThisService(){
        Map<String, String> record = new HashMap<String, String>();
        record.put(TXTRECORD_PROP_AVAILABLE, "visible");
        record.put(TestMapping, mappingTextButton.getText().toString() );

        service = WifiP2pDnsSdServiceInfo.newInstance(
                SERVICE_INSTANCE, SERVICE_REG_TYPE, record);
        manager.addLocalService(channel, service, createWifiManagerListener("Added Local Service", "Failed to add a service"));
    }

    private void stopThisService(){
        if( service != null )
        {
            manager.removeLocalService(channel, service, createWifiManagerListener( "Removed Local Service", "Failed to remove a service" ));
        }
    }

    private void startDiscovery(){

        WifiP2pManager.DnsSdServiceResponseListener dnsSdServiceResponseListener = new WifiP2pManager.DnsSdServiceResponseListener() {

            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {

                // A service has been discovered. Is this our app?

                if (instanceName.equalsIgnoreCase(SERVICE_INSTANCE)) {

                    // update the UI and add the item the discovered
                    // device.

                    Log.d(TAG, "onBonjourServiceAvailable " + instanceName);

                    deviceList.add(srcDevice);
                    adapter.notifyDataSetChanged();

                    appendStatus("added " + srcDevice.deviceName + " to Device List");

                }
            }
        };

        WifiP2pManager.DnsSdTxtRecordListener dnsSdTxtRecordListener = new WifiP2pManager.DnsSdTxtRecordListener() {

            /**
             * A new TXT record is available. Pick up the advertised
             * buddy name.
             */
            @Override
            public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> record, WifiP2pDevice device) {
                appendStatus(record.get(TestMapping));
                Log.d(TAG, device.deviceName + " is " + record.get(TXTRECORD_PROP_AVAILABLE));
            }
        };

        manager.setDnsSdResponseListeners( channel, dnsSdServiceResponseListener, dnsSdTxtRecordListener );

        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance( );
        manager.addServiceRequest(channel, serviceRequest, createWifiManagerListener("Added service discovery request", "Failed adding service discovery request") );
        manager.discoverServices(channel, createWifiManagerListener( "Service discovery initiated", "Service discovery failed" ) );

    }

    private void stopDiscovery(){
        manager.clearServiceRequests(channel, createWifiManagerListener("Stop discovery", "Stop discovery failed"));
    }

    private int connectWithPeer(WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;


        manager.connect( channel, config, createWifiManagerListener( "Connecting to service", "Failed connecting to service" ) );
        // TODO: später bei failure einfach repitition

        return 0;
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

    private WifiP2pManager.ActionListener createWifiManagerListener( final String onSuccessMsg, final String onFailureMsg ){
        return new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                appendStatus( onSuccessMsg );
            }

            @Override
            public void onFailure(int error) {
                appendStatus( onFailureMsg );
            }
        };
    }

    private void appendStatus(String status) {
        Toast.makeText( this,status,Toast.LENGTH_SHORT ).show();
    }


    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        appendStatus("connection established");

    }
}
