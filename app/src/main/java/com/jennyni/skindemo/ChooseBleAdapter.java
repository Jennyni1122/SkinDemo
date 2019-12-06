package com.jennyni.skindemo;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 *
 * Created by Jenny on 2019/7/16.
 */

public class ChooseBleAdapter extends BaseAdapter {
    public static final String TAG = "ChooseBleAdapter";
    private Context context;
    private final List<BluetoothDevice> leDeviceList;

    public ChooseBleAdapter(Context context, List<BluetoothDevice> leDeviceList){
        this.context = context;
        this.leDeviceList = leDeviceList;
    }

    @Override
    public int getCount() {
        return leDeviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return leDeviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        LayoutInflater mInflater = LayoutInflater.from(context);
        if (convertView == null){
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_ble_list,parent,false);
            viewHolder.tv_bleName = convertView.findViewById(R.id.tv_ble_name);
            viewHolder.tv_bleAddress = convertView.findViewById(R.id.tv_ble_address);
            convertView.setTag(viewHolder);
        }else {
            Log.d(TAG,"not null" + position);
            viewHolder = (ViewHolder)convertView.getTag();
        }

        //为列表项赋值
        BluetoothDevice device = leDeviceList.get(position);
        viewHolder.device = device;
        final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0){
            viewHolder.tv_bleName.setText(device.getName());
        }else {
            viewHolder.tv_bleName.setText("Unknown Device");
        }
        viewHolder.tv_bleAddress.setText(device.getAddress());
        return convertView;
    }

    private class ViewHolder{
        public BluetoothDevice device;      //不是用来显示，用来在item点击时返回连接对象
        TextView tv_bleName,tv_bleAddress;
    }
}
