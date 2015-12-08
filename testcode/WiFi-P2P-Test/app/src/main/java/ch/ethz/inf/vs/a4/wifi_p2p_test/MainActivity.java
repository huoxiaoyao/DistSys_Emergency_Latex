package ch.ethz.inf.vs.a4.wifi_p2p_test;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager mManager;
    private WifiDirectBroadcastReceiver receiver;
    final HashMap<String, String> buddies = new HashMap<String, String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //  Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);


    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    /** register the BroadcastReceiver with the intent values to be matched */
    @Override
    public void onResume() {
        super.onResume();
        /*receiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this);
        registerReceiver(receiver, intentFilter);*/
    }

    @Override
    public void onPause() {
        super.onPause();
        //unregisterReceiver(receiver);
    }

    public void onInitClicked(View view){
        receiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this);
        registerReceiver(receiver, intentFilter);

        discoverService();
    }

    public void onCancelClicked(View view) {
        unregisterReceiver(receiver);
        mManager.clearLocalServices(mChannel, null);
    }

    public void setIsWifiP2pEnabled(boolean enabled) {
        Toast.makeText(this, "WifiP2PEnabled: " + String.valueOf(enabled), Toast.LENGTH_SHORT).show();
    }

    private void discoverService() {
        //possibly need this, did not test yet
        mManager.clearServiceRequests(mChannel, null);
        mManager.clearLocalServices(mChannel, null);


        WifiP2pManager.DnsSdTxtRecordListener txtListener = new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
            public void onDnsSdTxtRecordAvailable(
                    String fullDomain, Map record, WifiP2pDevice device) {
                Log.d("YEAH", "DnsSdTxtRecord available -" + record.toString());
                Toast.makeText(MainActivity.this, "TXT: " + device.deviceName, Toast.LENGTH_LONG).show();
                buddies.put(device.deviceAddress, (String)record.get("buddyName"));
            }
        };

        WifiP2pManager.DnsSdServiceResponseListener servListener =
                new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType,
                                                WifiP2pDevice resourceType) {

                // Update the device name with the human-friendly version from
                // the DnsTxtRecord, assuming one arrived.
                resourceType.deviceName = buddies
                        .containsKey(resourceType.deviceAddress) ? buddies
                        .get(resourceType.deviceAddress) : resourceType.deviceName;

                //for testing purposes, just add the name to a list
                Toast.makeText(MainActivity.this, "found device" + resourceType.deviceName, Toast.LENGTH_LONG).show();


                /*// Add to the custom adapter defined specifically for showing
                // wifi devices.
                WiFiDirectServicesList fragment = (WiFiDirectServicesList) getFragmentManager()
                        .findFragmentById(R.id.frag_peerlist);
                WiFiDevicesAdapter adapter = ((WiFiDevicesAdapter) fragment
                        .getListAdapter());

                adapter.add(resourceType);
                adapter.notifyDataSetChanged();
                Log.d(TAG, "onBonjourServiceAvailable " + instanceName);*/
            }
        };

        mManager.setDnsSdResponseListeners(mChannel, servListener, txtListener);

        WifiP2pDnsSdServiceRequest req = WifiP2pDnsSdServiceRequest.newInstance(); //WifiP2pDnsSdServiceRequest.newInstance("_save_my_ass._tcp");

        mManager.addServiceRequest(mChannel, req, new WifiP2pManager.ActionListener() {
            String text;

            @Override
            public void onSuccess() {
                text = "adding worked";
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                text = "adding failed";
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });


        mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // Success!
            }

            @Override
            public void onFailure(int code) {
                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                String text;
                if (code == WifiP2pManager.P2P_UNSUPPORTED) {
                    text = "P2P isn't supported on this device.";
                } else if (code == WifiP2pManager.BUSY) {
                    text = "too busy for discovery";
                } else if (code == WifiP2pManager.ERROR) {
                    text = "ERRRRROOOOORR discovering";
                } else {
                    text = "STUUUUPID UNREGISTERED: " + String.valueOf(code);
                }
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
                Log.d("STUPID", text);
            }

            });

    }
}
