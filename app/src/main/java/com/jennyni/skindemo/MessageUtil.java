package com.jennyni.skindemo;

import android.util.Log;

import java.util.Arrays;

/**
 *
 * Created by Jenny on 2019/7/16.
 */

public class MessageUtil {

    //皮电
    static final byte[] RECIVER_DATA = {0x00};
    static final byte[] CLOSE_DATA = {0x01};

    //膀胱
    static final byte[] RECIVER_DATA1 = {0x03};
    static final byte[] CLOSE_DATA1 = {0x04};
    //ble
    static final byte[] Urine_CHECK = {(byte)0x0C,(byte)0x89,(byte) 0xA5,(byte) 0xD5,(byte)0xFF,0x11,0x01,0x00};
    /**
     * 接收数据
     * @return
     */
    static byte[] getReciverData() {
        return RECIVER_DATA;
    }

    /**
     * 关闭数据
     * @return
     */
    static byte[] getCloseData() {
        return CLOSE_DATA;
    }


    //膀胱
    /**
     * 接收数据
     * @return
     */
    static byte[] getReciverData1() {
        return RECIVER_DATA1;
    }



    /**
     * 关闭数据
     * @return
     */
    static byte[] getCloseData1() {
        return CLOSE_DATA1;
    }




    /**
     * 是否膀胱
     *
     * @param values
     * @return
     */

    public static boolean isUrine(byte[] values) {
        byte[] header = splitByte(8, values, 0);
        printData(header);
        return values == null ? false : Arrays.equals(header, Urine_CHECK);
    }

    /**
     * 获取膀胱的数据部分
     * 原数据 640bit 长度：160 截取：32bit  长度：8
     * @param values
     * @return
     */

    public static byte[] getUrineData(byte[] values) {
        if (values.length < 160) {
            Log.e("BLECommon", "数据不符合要求");
            return null;
        }
        return splitByte(152, values, 8);
    }

    /**
     * 快速截取数组的某一段。
     * @param size   新数组的长度
     * @param parent 要被切割的数组
     * @param begin  要被切割的数组的起始位置
     * @return
     */
    public static byte[] splitByte(int size, byte[] parent, int begin) {
        byte[] bytes = new byte[size];
        System.arraycopy(parent, begin, bytes, 0, bytes.length);
        return bytes;
    }


    static void printData(byte[] b) {
        Log.e("BLECommon", b == null ? "数据 b 为null" : bytesToHexString(b));
    }

    /**
     * 把字节数组转换成16进制字符串
     * @param bArray
     * @return
     */
    public static final String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

}
