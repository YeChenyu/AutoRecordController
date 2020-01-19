package com.view.core.utils;

import java.util.Locale;

public class HexDump {
	private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	public static String dumpHexString(byte[] array) {
		return dumpHexString(array, 0, array.length);
	}

	public static String dumpHexString(byte[] array, int offset, int length) {
		StringBuilder result = new StringBuilder();

		byte[] line = new byte[16];
		int lineIndex = 0;

		result.append("\n0x");
		result.append(toHexString(offset));

		for (int i = offset; i < offset + length; i++) {
			if (lineIndex == 16) {
				result.append(" ");

				for (int j = 0; j < 16; j++) {
					if ((line[j] > 32) && (line[j] < 126))
						result.append(new String(line, j, 1));
					else {
						result.append(".");
					}
				}

				result.append("\n0x");
				result.append(toHexString(i));
				lineIndex = 0;
			}

			byte b = array[i];
			result.append(" ");
			result.append(HEX_DIGITS[(b >>> 4 & 0xF)]);
			result.append(HEX_DIGITS[(b & 0xF)]);

			line[(lineIndex++)] = b;
		}

		if (lineIndex != 16) {
			int count = (16 - lineIndex) * 3;
			count++;
			for (int i = 0; i < count; i++) {
				result.append(" ");
			}

			for (int i = 0; i < lineIndex; i++) {
				if ((line[i] > 32) && (line[i] < 126))
					result.append(new String(line, i, 1));
				else {
					result.append(".");
				}
			}
		}

		return result.toString();
	}

	public static String toHexString(byte b) {
		return toHexString(toByteArray(b));
	}

	public static String toHexString(byte[] array) {
		return toHexString(array, 0, array.length);
	}

	public static String toHexString(byte[] array, int offset, int length) {
		char[] buf = new char[length * 2];

		int bufIndex = 0;
		for (int i = offset; i < offset + length; i++) {
			byte b = array[i];
			buf[(bufIndex++)] = HEX_DIGITS[(b >>> 4 & 0xF)];
			buf[(bufIndex++)] = HEX_DIGITS[(b & 0xF)];
		}

		return new String(buf);
	}

	public static String toHexString(int i) {
		return String.format(Locale.US,"%08X", i);// toHexString(toByteArray(i));
	}

	public static byte[] toByteArray(byte b) {
		byte[] array = new byte[1];
		array[0] = b;
		return array;
	}

	public static byte[] toByteArray(int i) {
		byte[] array = new byte[4];

		array[3] = ((byte) (i & 0xFF));
		array[2] = ((byte) (i >> 8 & 0xFF));
		array[1] = ((byte) (i >> 16 & 0xFF));
		array[0] = ((byte) (i >> 24 & 0xFF));

		return array;
	}

	private static int toByte(char c) {
		if ((c >= '0') && (c <= '9'))
			return c - '0';
		if ((c >= 'A') && (c <= 'F'))
			return c - 'A' + 10;
		if ((c >= 'a') && (c <= 'f')) {
			return c - 'a' + 10;
		}
		throw new RuntimeException("Invalid hex char '" + c + "'");
	}

	public static byte[] hexStringToByteArray(String hexString) {
		int length = hexString.length();
		byte[] buffer = new byte[length / 2];

		for (int i = 0; i < length; i += 2) {
			buffer[(i / 2)] = ((byte) (toByte(hexString.charAt(i)) << 4 | toByte(hexString
					.charAt(i + 1))));
		}

		return buffer;
	}

	public static byte[] hexStringToByteArray64(String hexString) {
		int length = hexString.length();
		byte[] buffer = new byte[64];

		for (int i = 0; i < length; i += 2) {
			buffer[(i / 2)] = ((byte) (toByte(hexString.charAt(i)) << 4 | toByte(hexString
					.charAt(i + 1))));
		}

		return buffer;
	}

//	/**
//	 * С������ byte������ȡint��ֵ��������������(��λ�ں󣬸�λ��ǰ)��˳�򡣺�intToBytes2��������ʹ��
//	 *
//	 * @param src
//	 *            byte����
//	 * @param offset
//	 *            ������ĵ�offsetλ��ʼ
//	 * @return
//	 */
	public static int bytesToInt(byte[] src, int offset) {
		int value;
		/*
		 * value = (int) ((src[offset+3] & 0xFF) | ((src[offset+2] & 0xFF)<<8) |
		 * ((src[offset+1] & 0xFF)<<16) | ((src[offset] & 0xFF)<<24));
		 */
		int a[] = new int[4];
		a[0] = (src[offset + 3] & 0xFF);
		a[1] = (src[offset + 2] & 0xFF) << 8;
		a[2] = (src[offset + 1] & 0xFF) << 16;
		a[3] = (src[offset] & 0xFF) << 24;
		value = a[0] | a[1] | a[2] | a[3];
		return value;
	}

//	/**
//	 * С������ ��int��ֵת��Ϊռ�ĸ��ֽڵ�byte���飬������������(��λ��ǰ����λ�ں�)��˳��
//	 */
	public static byte[] intToBytes2(int value) {
		byte[] src = new byte[4];
		src[0] = (byte) ((value >> 24) & 0xFF);
		src[1] = (byte) ((value >> 16) & 0xFF);
		src[2] = (byte) ((value >> 8) & 0xFF);
		src[3] = (byte) (value & 0xFF);
		return src;
	}

//	/**
//	 * ������� byte������ȡint��ֵ��������������(��λ��ǰ����λ�ں�)��˳�򣬺ͺ�intToBytes��������ʹ�� *
//	 *
//	 * @param src
//	 *            byte����
//	 * @param offset
//	 *            ������ĵ�offsetλ��ʼ
//	 * @return
//	 */
	public static int bytesToint(byte[] src, int offset) {
		int value;
		value = (src[offset] & 0xFF) | ((src[offset + 1] & 0xFF) << 8)
				| ((src[offset + 2] & 0xFF) << 16) | ((src[offset + 3] & 0xFF) << 24);
		return value;
	}

//	/**
//	 * ������� ��int��ֵת��Ϊռ�ĸ��ֽڵ�byte���飬������������(��λ��ǰ����λ�ں�)��˳��
//	 *
//	 * @param value
//	 *            Ҫת����intֵ
//	 * @return byte����
//	 */
	public static byte[] intToBytes(int value) {
		byte[] src = new byte[4];
		src[3] = (byte) ((value >> 24) & 0xFF);
		src[2] = (byte) ((value >> 16) & 0xFF);
		src[1] = (byte) ((value >> 8) & 0xFF);
		src[0] = (byte) (value & 0xFF);
		return src;
	}

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
		return new String(bytes);
	}
}