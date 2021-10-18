package com.auto.commonlibrary.util;

public class LengthUtil {
	
	
	public static byte[] len2LLVAR(int len) {
		byte[] llvar = new byte[2];
		llvar[0] = HexUtil.ByteToBcd(len / 100);
		int len1 = len % 100;
		llvar[1] = HexUtil.ByteToBcd(len1);
		return llvar;
	}

	
	
	public static int llvar2Len(byte up, byte low) {
		return Integer.valueOf(StringUtil.byte2HexStr(new byte[] { up, low }));
	}
	
	public static int llvar2Len(byte[] llvar) {
		return Integer.valueOf(StringUtil.byte2HexStr(llvar));
	}
	
}
