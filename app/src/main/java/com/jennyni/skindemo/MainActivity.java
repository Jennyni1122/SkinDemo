package com.jennyni.skindemo;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.jennyni.skindemo.Constants.UUID_CONFIG;
import static com.jennyni.skindemo.Constants.UUID_CONFIG1;
import static com.jennyni.skindemo.Constants.UUID_READ;
import static com.jennyni.skindemo.Constants.UUID_READ1;
import static com.jennyni.skindemo.Constants.UUID_SERVER;
import static com.jennyni.skindemo.Constants.UUID_SERVER1;
import static com.jennyni.skindemo.Constants.UUID_WRITE;
import static com.jennyni.skindemo.Constants.UUID_WRITE1;

public class MainActivity extends Activity implements View.OnClickListener{

    public static final String TAG = "BLETest ";
    public static final String DEVICE_BLE = "device_ble";

    private TextView tv_main_title, tv_switch, tv_back;
    private RelativeLayout rl_title_bar;
    private TextView tv_ble_address, tv_status,tv_pw_values,tv_pd_values,tv_ble_values,tv_accX,tv_accY,tv_accZ;
    private Button start_heart_test;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic readCharacteristic;
    private BluetoothGattCharacteristic writeCharacteristic;
    private boolean isConnect;
    private String id,testName,testTime,testResult;
    private boolean flag = false;

    public static void startActivity(Context context, BluetoothDevice device) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(DEVICE_BLE, device);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initTitle();
        initView();
        setListener();
        setViewFunction();
    }



    private void initView() {
        tv_pw_values= (TextView) findViewById(R.id. tv_pw_values);
        tv_pd_values= (TextView) findViewById(R.id. tv_pd_values);
        tv_accX = (TextView) findViewById(R.id. tv_accX);
        tv_accY = (TextView) findViewById(R.id. tv_accY);
        tv_accZ = (TextView) findViewById(R.id. tv_accZ);
        tv_ble_values = (TextView) findViewById(R.id.tv_ble_values);
        tv_status = (TextView) findViewById(R.id.tv_status);
        start_heart_test = (Button) findViewById(R.id.start_heart_test);

    }

    private void initTitle() {
        //标题栏
        tv_main_title = (TextView) findViewById(R.id.tv_main_title);
        rl_title_bar = (RelativeLayout) findViewById(R.id.title_bar);
        rl_title_bar.setBackgroundColor(getResources().getColor(R.color.rdTextColorPress));
        tv_switch = (TextView) findViewById(R.id.tv_save);
        tv_switch.setVisibility(View.VISIBLE);
        tv_switch.setText("选择蓝牙");
    }

    private void setViewFunction() {
        BluetoothDevice device = getIntent().getParcelableExtra(DEVICE_BLE);
        bluetoothGatt = device.connectGatt(MainActivity.this, false, gattCallback);
        if (isConnect = true) {
            tv_main_title.setText(device.getName());
        } else {
            tv_main_title.setText("蓝牙测试");
        }
    }

    private void setListener() {
        tv_switch.setOnClickListener(this);
        start_heart_test.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_save:           //跳转 选择蓝牙列表界面
                Intent chooseintent = new Intent(MainActivity.this, ChooseBleActivity.class);
                startActivity(chooseintent);
                break;
            case R.id.start_heart_test: //开始测试
                if (!isConnect) {
                    Toast.makeText(MainActivity.this, "请先连接设备~", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    if ("开始".equals(start_heart_test.getText().toString())) {
                        flag = true;
                        start_heart_test.setText("停止");
                        //接收原始数据   00
                        sendDatatoBle(MessageUtil.getReciverData());

                        // getEcgData(values);
                    } else {
                        flag = false;
                        start_heart_test.setText("开始");
                        //01
                        sendDatatoBle(MessageUtil.getCloseData());

                    }
                }

                break;
                }
    }

    private void sendDatatoBle(byte[] value) {
        writeCharacteristic.setValue(value);
        bluetoothGatt.writeCharacteristic(writeCharacteristic);
    }

    //连接设备
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.e(TAG, "onConnectionStateChang：status ==>" + newState);
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                bluetoothGatt.discoverServices();
            //    tv_status.setText("已连接");
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                //断开设备  disconnectDevice();
                if (bluetoothGatt != null) {
                    bluetoothGatt.close();
                    isConnect = false;
                     tv_status.setText("未连接");
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.e(TAG, "onServicesDiscovered: ");
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {      //已经连接上设备
                // tv_status.setText("已连接");
                //printServiceAllProfile(supportedGattServices);
                BluetoothGattService service = gatt.getService(UUID.fromString(UUID_SERVER));
                readCharacteristic = service.getCharacteristic(UUID.fromString(UUID_READ));
                writeCharacteristic = service.getCharacteristic(UUID.fromString(UUID_WRITE));
                gatt.setCharacteristicNotification(readCharacteristic, true);
                //getDescriptor
                BluetoothGattDescriptor descriptor = getDescriptor(UUID.fromString(UUID_CONFIG));
                if (descriptor != null) {
                    //蓝牙通信回调，然后回调通信内容会回调到onCharacteristicChanged
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);
                    isConnect = true;
//                        sendData2Bluetooth(RECIVER_DATA);//////刚连接设备最好延迟发送数据，不然可能会像昨天那样蓝牙连接莫名断掉
                } else {
                    Log.e(TAG, "onServicesDiscovered:descriptor==null ");
//                    tv_status.setText("已断开");
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            //这个方法我还没试过回调的，不知道什么情况下会调用
            byte[] values = characteristic.getValue();
            //    printData(values);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            //一般发送给蓝牙的数据格式不对的话蓝牙设备会把发送的数据返回到这个方法，调通之后这个方法就不会回调了
            byte[] values = characteristic.getValue();
            printData(values); //输出接收00，停止01
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            //发送数据给蓝牙，蓝牙的信息返回在此处
            byte[] values = characteristic.getValue();
            printData(values); //输出原始数据

        }
    };

    public static final ExecutorService EXECUTORS= Executors.newSingleThreadExecutor();

    private void printData(final byte[] values) {
        Log.e(" printData==>", values == null ? "数据 values 为null" : ConvertUtils.bytesToHexString(values));



        //皮电
           try{
               //数据处理
               EXECUTORS.execute(new Runnable() {
                   @Override
                   public void run() {
                       for (int i=0;i < values.length;i++){
                           //心率值
                           if (values.length < 2) return;
                           if ((values[0] == 0x01) && (values[1] == 0x01)) {
                               int hr;
                               hr = values[2] & 0xff;

                               final String hr_value = String.valueOf(hr);
                               Log.i("==心电hr==", hr_value);
                               runOnUiThread(new Runnable() {
                                   @Override
                                   public void run() {
                                       tv_ble_values.setText(String.valueOf(hr_value));
                                   }
                               });

                           }
                           //皮温
                           if ((values[0] == 0x02) && (values[1] == 0x02)) {
                               double temp;
                               int sum;
                               sum = ((values[3] & 0x00ff) << 8) | (values[2] & 0xff);
                               temp = 0.02 * sum - 273.5;
                               DecimalFormat df_temp = new DecimalFormat("#.00");
                               final String temp_value = String.valueOf(df_temp.format(temp));
                               Log.i("==皮温temp==",temp_value);
                               runOnUiThread(new Runnable() {
                                   @Override
                                   public void run() {
                                       tv_pw_values.setText(temp_value);
                                   }
                               });

                           }
                           //皮电
                           if ((values[0] == 0x03) && (values[1] == 0x03)) {
                               int eda1, eda2;
                               double r, s;
                               r = s = 0;
                               eda1 = ((0x0f & values[2]) << 8) | (0xff & values[3]);
                               eda2 = ((0x0f & values[4]) << 8) | (0xff & values[5]);
                               r = 500 * eda2 / (2 * eda1 - eda2);
                               s = 1000 / r;

                               DecimalFormat df_eda = new DecimalFormat("#.00");
                               final String eda_s_value = String.valueOf(df_eda.format(s));

                               Log.i("==电阻eda_r==", String.valueOf(df_eda.format(r)));
                               Log.i("==皮电eda_s==", eda_s_value);
                               runOnUiThread(new Runnable() {
                                   @Override
                                   public void run() {
                                       tv_pd_values.setText(eda_s_value);
                                   }
                               });
                           }

                           //三轴
                           if ((values[0] == 0x04) && (values[1] == 0x04)) {
                               int x, y, z;
                               if ((values[3] & 0x80) == 0x80) {
                                   x = (((values[3] & 0x7f) << 8) | (values[2] & 0xff));
                                   x = x >> 4;
                                   x = -x * 4000 / 2048 / 2;
                               } else {
                                   x = (((values[3] & 0xff) << 8) | (values[2] & 0xff));
                                   x = x >> 4;
                                   x = x * 4000 / 2048 / 2;
                               }
                               if ((values[5] & 0x80) == 0x80) {
                                   y = (((values[5] & 0xff) << 24) | ((values[4] & 0xff)) << 16);
                                   y = y >> 20;
                                   y = y * 4000 / 2048 / 2;

                               } else {
                                   y = (((values[5] & 0xff) << 8) | (values[4] & 0xff));
                                   y = y >> 4;
                                   y = y * 4000 / 2048 / 2;
                               }
                               if ((values[7] & 0x80) == 0x80) {
                                   z = (((values[7] & 0x7f) << 8) | (values[6] & 0xff));
                                   z = z >> 4;
                                   z = -z * 4000 / 2048 / 2;
                               } else {
                                   z = (((values[7] & 0xff) << 8) | (values[6] & 0xff));
                                   z = z >> 4;
                                   z = z * 4000 / 2048 / 2;
                               }
                                final String x_value = String.valueOf(x);
                                final String y_value = String.valueOf(y);
                                final String z_value = String.valueOf(z);

                               Log.i("==acc_x==",x_value);
                               Log.i("==acc_y==",y_value);
                               Log.i("==acc_z==",z_value);
                               runOnUiThread(new Runnable() {
                                   @Override
                                   public void run() {
                                       tv_accX.setText(String.valueOf(x_value));
                                       tv_accY.setText(String.valueOf(y_value));
                                       tv_accZ.setText(String.valueOf(z_value));

                                   }
                               });

                           }


                       }
                   }
               });

           }catch (Exception e){
               Log.e(TAG,"输出异常");
           }




    }

    BluetoothGattDescriptor getDescriptor(UUID str) {
        return readCharacteristic.getDescriptor(str);
    }


    public static void printServiceAllProfile(List<BluetoothGattService> supportedGattServices) {
        // 循环遍历服务以及每个服务下面的各个特征，判断读写，通知属性
        for (BluetoothGattService gattService :supportedGattServices) {
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                int charaProp = gattCharacteristic.getProperties();
                boolean isRead = false;
                boolean isWrite = false;
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                    Log.e("nihao","gattCharacteristic的UUID为:"+gattCharacteristic.getUuid());
                    Log.e("nihao","gattCharacteristic的属性为:  可读");
                    isRead = true;
                }
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                    Log.e("nihao","gattCharacteristic的UUID为:"+gattCharacteristic.getUuid());
                    Log.e("nihao","gattCharacteristic的属性为:  可写");
                    isWrite = true;
                }
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    Log.e("nihao","gattCharacteristic的UUID为:"+gattCharacteristic.getUuid());
                    Log.e("nihao","gattCharacteristic的属性为:  具备通知属性");
                }
//                if (isRead && isWrite) {
//                 Log.e("nihao","gattCharacteristic的UUID为:"+gattCharacteristic.getUuid());
//                 Log.e("nihao","gattCharacteristic的属性为: 可读/可写");
//                }
            }
        }
    }

}
