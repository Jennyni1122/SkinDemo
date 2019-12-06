package com.jennyni.skindemo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.jennyni.skindemo.Constants.UUID_CONFIG;
import static com.jennyni.skindemo.Constants.UUID_CONFIG1;
import static com.jennyni.skindemo.Constants.UUID_READ;
import static com.jennyni.skindemo.Constants.UUID_READ1;
import static com.jennyni.skindemo.Constants.UUID_SERVER;
import static com.jennyni.skindemo.Constants.UUID_SERVER1;
import static com.jennyni.skindemo.Constants.UUID_WRITE;
import static com.jennyni.skindemo.Constants.UUID_WRITE1;

public class ChooseBleActivity extends PermissionsActivity {

    public static final String TAG = "ChooseBle";
    private TextView tv_main_title, tv_switch, tv_back;
    private RelativeLayout rl_title_bar;
    private ListView lv_ble;
    private ImageView iv_close;
    private List<BluetoothDevice> deviceList = new ArrayList<>();
    private List<String> deviceAddress = new ArrayList<>();
    private ChooseBleAdapter chooseBleAdapter;
    private BluetoothGattCharacteristic readCharacteristic;
    private BluetoothGattCharacteristic writeCharacteristic;
    private boolean isConnect;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;

    /**
     * 扫描设备回调
     */
    private BluetoothAdapter.LeScanCallback mScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.e(TAG,"name:" + device.getName() + ",mac:" + device.getAddress());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //过滤重复的地址
                    if (!deviceAddress.contains(device.getAddress())) {
                        deviceList.add(device);
                        deviceAddress.add(device.getAddress());
                        chooseBleAdapter.notifyDataSetChanged();
                    }
                }
            });


        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_ble);
        initTitle();
        initView();
    }

    private void initTitle() {
        //标题栏
        tv_main_title = (TextView) findViewById(R.id.tv_main_title);
        rl_title_bar = (RelativeLayout) findViewById(R.id.title_bar);
        rl_title_bar.setBackgroundColor(getResources().getColor(R.color.rdTextColorPress));
        tv_back = (TextView) findViewById(R.id.tv_back);
        tv_switch = (TextView) findViewById(R.id.tv_save);
        tv_main_title.setText("选择蓝牙设备");
    }


    private void initView() {
        lv_ble = (ListView) findViewById(R.id.lv_ble);
        chooseBleAdapter = new ChooseBleAdapter(this, deviceList);
        lv_ble.setAdapter(chooseBleAdapter);
        //Android版本小于23
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            //初始化蓝牙
            initBlueTooth();
            //开始扫描
            startScan();
        }
        //蓝牙列表的点击事件
        lv_ble.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //连接前先停止蓝牙扫描
                bluetoothAdapter.stopLeScan(mScanCallback);
                //点击蓝牙设备开始连接
                BluetoothDevice device = deviceList.get(position);
//                Log.e(TAG, "连接的设备为：" +( (device==null || device.getName() == null) ? "未知" : device.getName()));
//                bluetoothGatt = device.connectGatt(ChooseBleActivity.this, false, gattCallback);
                if (device!=null){
                    MainActivity.startActivity(ChooseBleActivity.this,device);
                    ChooseBleActivity.this.finish();
                }

            }
        });
    }


        /**
         * 初始化蓝牙
         */
    private void initBlueTooth() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }


    /**
     * 开始蓝牙扫描
     */
    private void startScan() {
        //判断蓝牙是否打开
        if ( bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.stopLeScan(mScanCallback);
            bluetoothAdapter.startLeScan(mScanCallback);
        } else {
            //强制打开蓝牙
            bluetoothAdapter.enable();
        }
    }

    //连接设备
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.e(TAG, "onConnectionStateChang：status ==>" + newState);
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                bluetoothGatt.discoverServices();
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                //断开设备  disconnectDevice();
                if (bluetoothGatt != null) {
                    bluetoothGatt.close();
                    isConnect = false;
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.e(TAG, "onServicesDiscovered: ");
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {      //已经连接上设备
                BluetoothGattService service = gatt.getService(UUID.fromString(UUID_SERVER1));
                readCharacteristic = service.getCharacteristic(UUID.fromString(UUID_READ1));
                writeCharacteristic = service.getCharacteristic(UUID.fromString(UUID_WRITE1));
                gatt.setCharacteristicNotification(readCharacteristic, true);
                //getDescriptor
                BluetoothGattDescriptor descriptor = getDescriptor(UUID.fromString(UUID_CONFIG1));
                if (descriptor != null) {
                    //蓝牙通信回调，然后回调通信内容会回调到onCharacteristicChanged
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);
                    isConnect = true;
//                        sendData2Bluetooth(RECIVER_DATA);//////刚连接设备最好延迟发送数据，不然可能会像昨天那样蓝牙连接莫名断掉
                } else {
                    Log.e(TAG, "onServicesDiscovered:descriptor==null ");
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            //这个方法我还没试过回调的，不知道什么情况下会调用
            byte[] values = characteristic.getValue();
            printData(values);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            //一般发送给蓝牙的数据格式不对的话蓝牙设备会把发送的数据返回到这个方法，调通之后这个方法就不会回调了
            byte[] values = characteristic.getValue();
            printData(values);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            //发送数据给蓝牙，蓝牙的信息返回在此处
            byte[] values = characteristic.getValue();
            printData(values);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);

        }
    };



    private void printData(byte[] values) {
        Log.e(TAG, values == null ? "数据 values 为null" : ConvertUtils.bytesToHexString(values));
    }

    BluetoothGattDescriptor getDescriptor(UUID str) {
        return readCharacteristic.getDescriptor(str);
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestPermission();
    }

    @Override
    public String[] getPermission() {
        return new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,

        };
    }

    @Override
    public void onPermissionRequestSuccess() {
        Log.e(TAG, "onPermissionRequestSuccess: ");
        //进行初始化蓝牙以及扫描工作
        initBlueTooth();
        startScan();
    }

    @Override
    public void onPermissionRequestFail() {
        Log.e(TAG, "onPermissionRequestFail: ");
    }
}
