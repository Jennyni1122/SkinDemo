package com.jennyni.skindemo;

/**
 * Created by Jenny on 2019/7/16.
 */

public class Constants {

    //蓝牙接口 （皮电）
    public static final String UUID_SERVER = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";  //6e400002-b5a3-f393-e0a9-e50e24dcca9e
    public static final String UUID_WRITE = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String UUID_READ = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String UUID_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";//在WRITE_NO_RESPONSE模式下 config UUID

    //蓝牙接口（膀胱）3写 2通知
    public static final String UUID_SERVER1 = "0000abf0-0000-1000-8000-00805f9b34fb";
    public static final String UUID_READ1 = "0000abf2-0000-1000-8000-00805f9b34fb";
    public static final String UUID_WRITE1 = "0000abf3-0000-1000-8000-00805f9b34fb";
    public static final String UUID_CONFIG1 = "00002902-0000-1000-8000-00805f9b34fb";//在WRITE_NO_RESPONSE模式下 config UUID


}
