package com.auto.commonlibrary.util;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class StringUtil {

    public static final String SEPARATOR_LINE = "|";
    public static final String SEPARATOR_LINE_SPLIT = "\\|";

    public static boolean isEmpty(String s) {
        if (s == null) {
            return true;
        } else if (s.trim().equals("")) {
            return true;
        } else return s.trim().equals("null");
    }

    public static boolean isNumber(String s) {

        try {
            Float.parseFloat(s);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    /**
     * src array to determine whether the data is the same as bt2 （begin from srcPos）
     *
     * @param src    source array
     * @param srcPos start point
     * @param bt2    target array
     * @param length length
     * @return
     */
    public static boolean isEqualsByte(byte[] src, int srcPos, byte[] bt2,
                                       int length) {

        byte[] temp = new byte[length];
        System.arraycopy(src, srcPos, temp, 0, length);

        return Arrays.equals(temp, bt2);

    }

    /**
     * string format(date time format)
     *
     * @param format   original format. yyyyMMdd HHmmss
     * @param toformat target format. yyyy-MM-dd HH:mm:ss
     * @param time     time
     * @return format result
     */
    public static String str2DateTime(String format, String toformat,
                                      String time) {
        String str = "";
        Date date;

        try {
            date = new SimpleDateFormat(format).parse(time);
            str = new SimpleDateFormat(toformat).format(date);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * short convert to byte
     *
     * @param s
     * @return
     */
    public static byte[] shortToByteArray(short s) {
        byte[] targets = new byte[2];
        // for (int i = 0; i < 2; i++) {
        // int offset = (targets.length - 1 - i) * 8;
        // targets[i] = (byte) ((s >>> offset) & 0xff);
        // }
        targets[0] = (byte) (s & 0x00ff);
        targets[1] = (byte) ((s & 0xff00) >> 8);
        return targets;
    }

    /**
     * short convert to byte
     *
     * @param s
     * @return
     */
    public static byte[] shortToByteArrayTwo(short s) {
        byte[] targets = new byte[2];
        // for (int i = 0; i < 2; i++) {
        // int offset = (targets.length - 1 - i) * 8;
        // targets[i] = (byte) ((s >>> offset) & 0xff);
        // }
        targets[1] = (byte) (s & 0x00ff);
        targets[0] = (byte) ((s & 0xff00) >> 8);
        return targets;
    }

    /**
     * short array convert to byte array
     *
     * @param s
     * @return
     */
    public static byte[] shortArrayToByteArray(short[] s) {
        byte[] targets = new byte[s.length * 2];
        for (int i = 0; i < s.length; i++) {
            byte[] tmp = shortToByteArray(s[i]);

            targets[2 * i] = tmp[0];
            targets[2 * i + 1] = tmp[1];
        }
        return targets;
    }

    /**
     * byte array convert to short array
     *
     * @param buf
     * @return
     */
    public static short[] byteArraytoShort(byte[] buf) {
        short[] targets = new short[buf.length / 2];
        short vSample;
        int len = 0;
        for (int i = 0; i < buf.length; i += 2) {
            vSample = (short) (buf[i] & 0x00FF);
            vSample |= (short) ((((short) buf[i + 1]) << 8) & 0xFF00);
            targets[len++] = vSample;
        }
        return targets;
    }

    //字符串转换成十六进制字符串
    // @param str 待转换数据
    //* @return String 每个Byte之间空格分隔，如: [61 6C 6B]

    /**
     * string convert to hexadecimal string
     *
     * @param str str  ASCII string to be converted
     * @return result
     */
    public static String str2HexStr(String str) {

        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;

        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    //十六进制转换字符串
    //* @param hexStr    strByte字符串(Byte之间无分隔符 如:[616C6B])

    /**
     * hexadecimal string convert to string
     *
     * @param hexStr byte string to be converted
     * @return String result
     */
    public static String hexStr2Str(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;

        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        try {
            return new String(bytes, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * @param str str Byte string(No separator between Byte,such as: [616C6B])
     * @return String string
     */
    public static byte[] str2bytesISO88591(String str) {
        try {
            return str.getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param str str Byte string(No separator between Byte,such as: [616C6B])
     * @return String 对应的字符串
     */
    public static byte[] str2bytesGBK(String str) {
        try {
            return str.getBytes("GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String byteToGBK(byte[] data) {
        String result = "";
        try {
            result = new String(data, "GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * Bytes converted to hexadecimal strings
     *
     * @param b byte array
     * @return result   Space separation between each Byte value
     */
    public static String byte2HexStr(byte[] b) {
        if (b == null)
            return "";
        String stmp = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
            // sb.append(" ");
        }
        return sb.toString().toUpperCase().trim();
    }

    public static String byte2HexStr(byte value) {
        byte[] b = new byte[]{value};
        String stmp = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
            // sb.append(" ");
        }
        return sb.toString().toUpperCase().trim();
    }

    /**
     * Bytes string to Byte value
     *
     * @param src src Byte string,there is no separator between each Byte
     * @return byte[]
     */
    public static byte[] hexStr2Bytes(String src) {
        if (TextUtils.isEmpty(src))
            return null;
        int m = 0, n = 0;
        if ((src.length() % 2) != 0)
            src = "0" + src;
        int l = src.length() / 2;
        // System.out.println(l);
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            m = i * 2 + 1;
            n = m + 1;
            ret[i] = Integer.decode(
                    "0x" + src.substring(i * 2, m) + src.substring(m, n))
                    .byteValue();
        }
        return ret;
    }

    /**
     * string convert to Unicode String
     *
     * @param strText strText full- width string
     * @return result there is no separator between each Unicode
     * @throws Exception
     */
    public static String strToUnicode(String strText) throws Exception {
        char c;
        StringBuilder str = new StringBuilder();
        int intAsc;
        String strHex;
        for (int i = 0; i < strText.length(); i++) {
            c = strText.charAt(i);
            intAsc = (int) c;
            strHex = Integer.toHexString(intAsc);
            if (intAsc > 128)
                str.append("\\u" + strHex);
            else
                // 低位在前面补00
                str.append("\\u00" + strHex);
        }
        return str.toString();
    }

    /**
     * unicode-String converts to string
     *
     * @param hex hex hexadecimal string（A Unicode is 2byte）
     * @return String full- width string
     */
    public static String unicodeToString(String hex) {
        int t = hex.length() / 6;
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < t; i++) {
            String s = hex.substring(i * 6, (i + 1) * 6);
            // 高位需要补上00再转
            String s1 = s.substring(2, 4) + "00";
            // 低位直接转
            String s2 = s.substring(4);
            // 将16进制的string转为int
            int n = Integer.valueOf(s1, 16) + Integer.valueOf(s2, 16);
            // 将int转换为字符
            char[] chars = Character.toChars(n);
            str.append(new String(chars));
        }
        return str.toString();
    }

    /**
     * @param src
     * @author Junhua Wu
     */
    public static int byteToInt(byte[] src) {
        int tmp = 0;
        for (int i = 0; i < src.length; i++) {
            tmp += ((int) src[i] << (i * 8)) & (0xFF << (i * 8));
        }

        return tmp;
    }

    /**
     * @param src
     * @author Junhua Wu
     */
    public static byte[] intToByte(int src) {
        byte[] tmp = new byte[4];
        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = (byte) ((src >> (i * 8)) & 0xFF);
        }
        return tmp;
    }

    public static byte[] intToBytes2(int value) {
        byte[] src = new byte[4];
        src[0] = (byte) ((value >> 24) & 0xFF);
        src[1] = (byte) ((value >> 16) & 0xFF);
        src[2] = (byte) ((value >> 8) & 0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }

    public static byte[] intToByte1024(int src) {
        byte[] tmp = new byte[1024];
        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = (byte) ((src >> (i * 8)) & 0xFF);
        }
        return tmp;
    }

    /**
     * @param src 16进制字符串
     * @return 字节数组
     * @throws
     * @Title:hexString2Bytes
     * @Description:16进制字符串转字节数组
     */
    public static byte[] hexString2Bytes(String src) {
        int l = src.length() / 2;
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            ret[i] = (byte) Integer
                    .valueOf(src.substring(i * 2, i * 2 + 2), 16).byteValue();
        }
        return ret;
    }

    public static String byteToStr(byte[] data) {
        String result = "";
        try {
            result = new String(data, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    /* *
     * hexadecimals string convert to a byte array
     * @param hex @return
     */
    public static byte[] hexStringToByte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    private static byte toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }

    /**
     * converts an array of bytes to hexadecimals strings
     *
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

    public static byte[] intToByteArray(int i) {
        byte[] targets = new byte[4];
        targets[0] = (byte) (i & 0xFF);
        targets[1] = (byte) ((i >> 8) & 0xFF);
        targets[2] = (byte) ((i >> 16) & 0xFF);
        targets[3] = (byte) ((i >> 24) & 0xFF);
        return targets;
    }

    /**
     * convert byte array to int type，little-endian mode
     *
     * @param b
     * @return
     */
    public static int byteArrayToInt(byte[] b) {
        int result = 0;
        result = (b[0] & 0xFF) | (b[1] << 8 & 0xFFFF) | (b[2] << 16 & 0xFFFFFF)
                | (b[3] << 24 & 0xFFFFFFFF);
        return result;
    }

    public static byte[] formatWithZero(byte[] data) {
        int mark = 0;
        for (int i = 0; i < data.length; i++) {
            if (data[i] == 0x00) {
                mark = i;
                break;
            }
        }
        if (mark == 0) {
            mark = data.length;
        }
        byte[] result = new byte[mark];
        System.arraycopy(data, 0, result, 0, mark);
        return result;
    }

    /**
     * formatted amount
     *
     * @param s   amount
     * @param len decimal digit
     * @return amount after format
     */
    public static String insertComma(String s, int len) {
        if (s == null || s.length() < 1) {
            return "";
        }
        NumberFormat formater = null;
        double num = Double.parseDouble(s);
        if (len == 0) {
            formater = new DecimalFormat("###,###");

        } else {
            StringBuffer buff = new StringBuffer();
            buff.append("###,###.");
            for (int i = 0; i < len; i++) {
                buff.append("#");
            }
            formater = new DecimalFormat(buff.toString());
        }
        return formater.format(num);
    }

    public static void main(String[] args) {
        System.out.println(byteToInt(new byte[]{0x00, 0x10}));
    }
}
