package com.view.core.utils;


/**
 * 
 * HexUtil
 *
 * @description hexadecimal tool
 */
public class HexUtil
{
	//字节数组转ASCII字符
	/**
	 * byte array to ASCII character
	 * @method toASCII
	 * @param data
	 * @return
	 */
	public static String toASCII(byte[] data) throws  IllegalArgumentException
	{
		// NULL处理
		if (null == data)
		{
			return null;
		}
		// 空处理
		if (data.length == 0)
		{
			return "";
		}
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < data.length; i++)
		{
			//对0xFF处理
			if(data[i]==-1)
			{
				buff.append("F");
			}
			else if(data[i]>=32&&data[i]<=126)
			{
				buff.append((char) data[i]);
			}
			else 
			{
//				throw new IllegalArgumentException("无法转为可见的ASCII字符");
				throw new IllegalArgumentException("Unable to convert visible ASCII character");
			}

		}
		return buff.toString();

	}

	//ASCII字符转字节数组
	/**
	 * ASCII character to byte array
	 * @method toByte
	 * @param asc ASCII character
	 * @return
	 */
	public static byte[] toByte(String asc)
	{
		
		// NULL处理
		if (null == asc||asc.trim().equals(""))
		{
			return null;
		}
		return asc.getBytes();
	}
	//BCD码转换成阿拉伯数字,例如有数组{0x12,0x34,0x56}转换为阿拉伯数字字符串后为123456
	/***
	 * The BCD code is converted into a Arabia number, such as an array of {0x12,0x34,0x56} converted to a Arabia number string is 123456
	 * @method toString
	 * @param bcd
	 * @return
	 */
	public static String toString(byte[] bcd)
	{

		// NULL处理
		if (null == bcd)
		{
			return null;
		}
		// 空处理
		if (bcd.length == 0)
		{
			return "";
		}

		StringBuffer buffer = new StringBuffer(bcd.length * 2);
		for (int i = 0; i < bcd.length; i++)
		{

			switch ((bcd[i] & 0xf0) >>> 4)
			{
			case 10:
				buffer.append("A");
				break;
			case 11:
				buffer.append("B");
				break;
			case 12:
				buffer.append("C");
				break;
			case 13:
				buffer.append("D");
				break;
			case 14:
				buffer.append("E");
				break;
			case 15:
				buffer.append("F");
				break;
			default:
				buffer.append((byte) ((bcd[i] & 0xf0) >>> 4));
				break;
			}
			switch (bcd[i] & 0x0f)
			{
			case 10:
				buffer.append("A");
				break;
			case 11:
				buffer.append("B");
				break;
			case 12:
				buffer.append("C");
				break;
			case 13:
				buffer.append("D");
				break;
			case 14:
				buffer.append("E");
				break;
			case 15:
				buffer.append("F");
				break;
			default:
				buffer.append((byte) (bcd[i] & 0x0f));
				break;
			}

		}
		return buffer.toString();
	}
	
	
	
	//是否为BCD字符串
	/**
	 * whether it is BCD string
	 * @method isBCD
	 * @param bcd BCD string
	 * @return
	 */
	public static boolean isBCD(String bcd)
	{
		boolean ans = false;
		// 化为大写
		bcd = bcd.toUpperCase();
		for (int i = 0; i < bcd.length(); i++)
		{
			ans = false;
			char c = bcd.charAt(i);
			if (c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' || c == '7' || c == '8' || c == '9' || 
				c == 'A' || c == 'B' || c == 'C' || c == 'D' || c == 'E' || c == 'F')
			{
				ans = true;
			}
			else 
			{
				break;
			}
		}
		return ans;
	}
	
	//ASCII字节数组转BCD编码
	/**
	 * ASCII byte array to BCD encoding
	 * @method toBCD
	 * @param asc   ASCII byte array
	 * @return
	 */
	public static byte[] toBCD(byte[] asc) throws IllegalArgumentException
	{
		// 空处理
		if (null == asc)
		{
			return null;
		}
		// 获得原始字节长度
		int len = asc.length;
		byte[] tmp = null;
		// 补位
		if (len % 2 != 0)
		{
			tmp = new byte[len + 1];
			//复制字节
			for (int i = 0; i < asc.length; i++)
			{
				tmp[i] = asc[i];
			}
			//补十六进制0
			tmp[len] =0x0;
			
		} 
		else
		{
			tmp = new byte[len];
			//复制字节
			for (int i = 0; i < asc.length; i++)
			{
				tmp[i] = asc[i];
			}
		}
		
		// 输出的bcd字节数组
		byte bcd[] = new byte[tmp.length / 2];

		
		int j, k;
		
		for (int p = 0; p < tmp.length / 2; p++)
		{
			// 双
			//处理0
			if(tmp[2 * p]==0x0)
			{
				j =0x0;
			}
			else if(tmp[2 * p]==0xf)
			{
				j =0xf;
			}
			//处理字符0-9
			else if ((tmp[2 * p] >= '0') && (tmp[2 * p] <= '9'))
			{
				j = tmp[2 * p] - '0';
			}
			//处理字符A
			else if (tmp[2 * p] == 'a' || tmp[2 * p] == 'A')
			{
				j = 0xa;
			}
			//处理字符B
			else if (tmp[2 * p] == 'b' || tmp[2 * p] == 'B')
			{
				j = 0xb;
			}
			//处理字符C
			else if (tmp[2 * p] == 'c' || tmp[2 * p] == 'C')
			{
				j = 0xc;
			}
			//处理字符D以及字符=
			else if (tmp[2 * p] == 'D' || tmp[2 * p] == 'd' || tmp[2 * p] == '=')
			{
				j = 0xd;
			}
			//处理字符E
			else if (tmp[2 * p] == 'e' || tmp[2 * p] == 'E')
			{
				j = 0xe;
			}
			//处理字符F
			else if (tmp[2 * p] == 'f' || tmp[2 * p] == 'F')
			{
				j = 0xf;
			}
			else
			{
//				throw new IllegalArgumentException("非BCD字节");
				throw new IllegalArgumentException("Non BCD bytes");
			}

			// 单
			//处理0x0
			if(tmp[2 * p + 1]==0x0)
			{
				k =0x0;
			}
			//处理0xf
			else if(tmp[2 * p + 1]==0xf)
			{
				k =0xf;
			}
			//处理字符0-9
			else if((tmp[2 * p + 1] >= '0') && (tmp[2 * p + 1] <= '9'))
			{
				k = tmp[2 * p + 1] - '0';
			}
			//处理字符A
			else if (tmp[2 * p + 1] == 'a' || tmp[2 * p + 1] == 'A')
			{
				k = 0xa;
			}
			//处理字符B
			else if (tmp[2 * p + 1] == 'b' || tmp[2 * p + 1] == 'B')
			{
				k = 0xb;
			}
			//处理字符C
			else if (tmp[2 * p + 1] == 'c' || tmp[2 * p + 1] == 'C')
			{
				k = 0xc;
			}
			//处理字符D以及字符=
			else if (tmp[2 * p + 1] == 'D' || tmp[2 * p + 1] == 'd' || tmp[2 * p + 1]  == '=' )
			{
				k = 0xd;
			}
			//处理字符E
			else if (tmp[2 * p + 1] == 'e' || tmp[2 * p + 1] == 'E')
			{
				k = 0xe;
			}
			//处理字符F
			else if (tmp[2 * p + 1] == 'f' || tmp[2 * p + 1] == 'F')
			{
				k = 0xf;
			}
			else
			{
//				throw new IllegalArgumentException("非BCD字节");
				throw new IllegalArgumentException("Non BCD bytes");
			}

			int a = (j << 4) + k;
			byte b = (byte) a;

			bcd[p] = b;
		}

		return bcd;

	}
	
	
	//字符串转化为BCD压缩码例如字符串"12345678",压缩之后的字节数组内容为{0x12,0x34,0x56,0x78}
	/**
	 * the string is converted to a BCD compression code such as the string "12345678", after which the compressed byte array is {0x12,0x34,0x56,0x78}
	 * @method toBCD
	 * @param asc   string
	 * @return
	 */
	public static byte[] toBCD(String asc) throws IllegalArgumentException
	{
		// 空处理
		if (null == asc || asc.trim().equals(""))
		{
			return null;
		}
		//替换=号
		asc = asc.replace('=', 'D').toUpperCase();
		// 非BCD编码处理
		if (!isBCD(asc))
		{
//			throw new IllegalArgumentException("非BCD字符串");
			throw new IllegalArgumentException("Non BCD string");
		}
		return toBCD(asc.getBytes());
	}

	//十进制Length转BCD字节,如:16转0x16
	/**
     * Decimal Length to BCD bytes, such as: 16 to 0x16
     * 
     * @param n
     * @return
     */
    public static byte ByteToBcd(int n) {
        int ret = ( (((n) / 10) << 4) | ((n) % 10));
        return (byte) (ret & 0xFF);
    }
	
	/**
	 * print BCD
	 * @param bcd BCD string
	 */
	public static void printBCD(String bcd)
	{
		//空处理
		if(null==bcd||bcd.trim().equals(""))
		{
//			throw new NullPointerException("打印的BCD字符串为NULL");
			throw new NullPointerException("BCD string to print is NULL");

		}
		if(!isBCD(bcd))
		{
//			throw new IllegalArgumentException("非BCD字符串");
			throw new IllegalArgumentException("Non BCD string");
		}
		for (int i = 0; i < bcd.length(); i++)
		{
			if(i>0&&i%2==0)
			{
				//打空格
				System.out.print(" ");
			}
			System.out.print(bcd.charAt(i));
			
		}
		System.out.println();
	}
	
	
	/**
	 * print BCD
	 * @param tip tag
	 * @param bcd BCD string
	 */
	public static void printBCD(String tip,String bcd)
	{
		//空处理
		if(null==bcd||bcd.trim().equals(""))
		{
//			throw new NullPointerException("打印的BCD字符串为NULL");
			throw new NullPointerException("BCD string to print is NULL");
		}
		if(!isBCD(bcd))
		{
//			throw new IllegalArgumentException("非BCD字符串");
			throw new IllegalArgumentException("Non BCD string");
		}
		System.out.print(tip);
		for (int i = 0; i < bcd.length(); i++)
		{
			if(i>0&&i%2==0)
			{
				//打空格
				System.out.print(" ");
			}
			System.out.print(bcd.charAt(i));
			
		}
		System.out.println();
	}


}
