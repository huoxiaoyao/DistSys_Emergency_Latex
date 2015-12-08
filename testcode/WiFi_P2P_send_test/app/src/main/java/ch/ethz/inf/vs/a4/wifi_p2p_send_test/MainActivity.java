package ch.ethz.inf.vs.a4.wifi_p2p_send_test;

import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ToggleButton t;
    WifiP2pDnsSdServiceInfo serviceInfo;
    WifiP2pManager mManager;
    WifiP2pManager.Channel channel;
    final int SERVER_PORT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        t = (ToggleButton)findViewById(R.id.toggle);

        mManager = (WifiP2pManager)getSystemService(WIFI_P2P_SERVICE);
        channel = mManager.initialize(this, getMainLooper(), null);

        //  Create a string map containing information about your service.
        Map record = new HashMap();
        record.put("listenport", String.valueOf(SERVER_PORT));
        record.put("buddyname", "John Doe" + (int) (Math.random() * 1000));
        record.put("available", "visible");

        // Service information.  Pass it an instance name, service type
        // _protocol._transportlayer , and the map containing
        // information other devices will want once they connect to this one.
        serviceInfo = WifiP2pDnsSdServiceInfo.newInstance("_test", "_save_my_ass._tcp", record);



        t.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    startSending();
                } else {
                    stopSending();
                }
            }
        });
    }

    private void startSending(){
        // Add the local service, sending the service info, network channel,
        // and listener that will be used to indicate success or failure of
        // the request.
        mManager.addLocalService(channel, serviceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // Command successful! Code isn't necessarily needed here,
                // Unless you want to update the UI or add logging statements.
                Toast.makeText(MainActivity.this, "Hosting works", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int arg0) {
                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                Toast.makeText(MainActivity.this, "Hosting failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void stopSending() {
        mManager.clearLocalServices(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "Cancel works", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this, "Cancel fail", Toast.LENGTH_SHORT).show();

            }
        });
    }
}
