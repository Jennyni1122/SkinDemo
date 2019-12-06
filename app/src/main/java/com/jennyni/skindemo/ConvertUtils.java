package com.jennyni.skindemo;

/**
 *
 * Created by Jenny on 2019/7/16.
 */

public class ConvertUtils {
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
