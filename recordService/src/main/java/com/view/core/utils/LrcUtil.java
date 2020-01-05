package com.view.core.utils;

public class LrcUtil {

    public static byte lrc_check(byte[] buf) {
        //Log.d("LrcUtil", "buf:"+StringUtil.byte2HexStr(buf));
        int iCount = 0;
        byte lrcValue = buf[0];
        
        //逐字节异或
        for (iCount = 1; iCount <buf.length; iCount++) {
           // Log.d("LrcUtil", "buf["+iCount+"]:"+StringUtil.byte2HexStr(new byte[]{buf[iCount]}));
            lrcValue = (byte) (lrcValue ^ buf[iCount]);
        }
        return lrcValue;


    }

    public static void main(String[] args) {

        byte[] buf = { 0x01, 0x05, 0x00, 0x00, 0x00, 0x01, (byte) 0xfc };
        byte lrc = 0x00;
       lrc = (byte) lrc_check(buf);
        System.out.println("lrc = :" +StringUtil.byte2HexStr(new byte[]{lrc}));

    }

}
