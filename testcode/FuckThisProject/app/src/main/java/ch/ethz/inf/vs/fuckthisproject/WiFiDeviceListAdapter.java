package ch.ethz.inf.vs.fuckthisproject;

import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Felix on 11.12.2015.
 */
public class WiFiDeviceListAdapter extends BaseAdapter{

    private List<WifiP2pDevice> deviceList;
    private LayoutInflater inflater;

    public WiFiDeviceListAdapter( List<WifiP2pDevice> deviceList, LayoutInflater inflater ){
        super();
        this.deviceList = deviceList;
        this.inflater = inflater;
    }

    @Override
    public int getCount() {
        return deviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return deviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return deviceList.get(position).hashCode();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder viewHolder;
        if ( view == null )
        {
            view = inflater.inflate(R.layout.listitem_device, null);
            viewHolder = new ViewHolder();
            viewHolder.deviceName = (TextView) view.findViewById(R.id.deviceTextView);
            view.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) view.getTag();
        }

        if ( deviceList.get(i) == null )
        {
            viewHolder.deviceName.setText("UNKNOWN");
        }
        else
        {
            viewHolder.deviceName.setText(deviceList.get(i).deviceName);
        }

        return view;
    }

    static class ViewHolder {
        TextView deviceName;
    }
}

