package com.view.core.utils;

public class BinaryUtils {

	private static String hexStr = "0123456789ABCDEF";
	private static String[] binaryArray = { "0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111", "1000",
			"1001", "1010", "1011", "1100", "1101", "1110", "1111" };

	public static void main(String[] args) {
		byte b = 20;//0x14  
		boolean[] bzw = new boolean[8];
		for (int i = 0, n = 1; i < 8; i++)
		{
		    bzw[i] = ((b & n) != 0);
		    n = n << 1;
		}
		for (int i = 0; i < bzw.length; i++) {
			System.out.println(bzw[i]+" ");
		}
		// String str = "二进制与十六进制互转测试";
		// System.out.println("源字符串：\n"+str);

		// String hexString = BinaryToHexString(str.getBytes());
		// System.out.println("转换为十六进制：\n"+hexString);
		System.out.println("转换为二进制：\n" + bytes2BinaryStr(new byte[] { (byte) 0xff }));

		// byte [] bArray = HexStringToBinary(hexString);
		// System.out.println("将str的十六进制文件转换为二进制再转为String：\n"+new
		// String(bArray));

	}

	/**
	 * convert to a binary string
	 * 
	 * @param bArray
	 * @return    binary string
	 */
	public static String bytes2BinaryStr(byte[] bArray) {

		String outStr = "";
		int pos = 0;
		for (byte b : bArray) {
			// 高四位
			pos = (b & 0xF0) >> 4;
			outStr += binaryArray[pos];
			// 低四位
			pos = b & 0x0F;
			outStr += binaryArray[pos];
		}
		return outStr;

	}

	/**
	 * Convert binary to hexadecimal string
	 *
	 * @param bytes
	 * @return   hexadecimal string
	 */
	public static String BinaryToHexString(byte[] bytes) {

		String result = "";
		String hex = "";
		for (int i = 0; i < bytes.length; i++) {
			// 字节高4位
			hex = String.valueOf(hexStr.charAt((bytes[i] & 0xF0) >> 4));
			// 字节低4位
			hex += String.valueOf(hexStr.charAt(bytes[i] & 0x0F));
			result += hex + " ";
		}
		return result;
	}

	/**
	 * converts hexadecimal string to byte arrays
	 * 
	 * @param hexString    hexadecimal string
	 * @return     byte arrays
	 */
	public static byte[] HexStringToBinary(String hexString) {
		// hexString的长度对2取整，作为bytes的长度
		int len = hexString.length() / 2;
		byte[] bytes = new byte[len];
		byte high = 0;// 字节高四位
		byte low = 0;// 字节低四位

		for (int i = 0; i < len; i++) {
			// 右移四位得到高位
			high = (byte) ((hexStr.indexOf(hexString.charAt(2 * i))) << 4);
			low = (byte) hexStr.indexOf(hexString.charAt(2 * i + 1));
			bytes[i] = (byte) (high | low);// 高地位做或运算
		}
		return bytes;
	}

	/**
	 * binary byte to byte string
	 * @param binaryByteString
	 * @return
     */
	public static byte[] byte2String(String binaryByteString) {
		// 假设binaryByte 是01，10，011，00以，分隔的格式的字符串
		int len = binaryByteString.length() / 8;
		String[] binaryStr = new String[len];
		for (int i = 0; i < binaryStr.length; i++) {
			binaryStr[i] = binaryByteString.substring(i * 8, (i + 1) * 8);
		}
		byte[] byteArray = new byte[binaryStr.length];
		for (int i = 0; i < byteArray.length; i++) {
			byteArray[i] = (byte) parse(binaryStr[i]);
		}
		return byteArray;
	}

	public static int parse(String str) {
		// 32位 为负数
		if (32 == str.length()) {
			str = "-" + str.substring(1);
			return -(Integer.parseInt(str, 2) + Integer.MAX_VALUE + 1);
		}
		return Integer.parseInt(str, 2);
	}
}
