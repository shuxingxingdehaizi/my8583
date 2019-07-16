package org.ethan.my8583;
/*
 * j8583 A Java implementation of the ISO8583 protocol
 * Copyright (C) 2007 Enrique Zamudio Lopez
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */


import java.io.UnsupportedEncodingException;

/** Utility class to perform HEX encoding/decoding of values.
 * 增加了encPack、encUnpack方法；
 * 从算法上看该编码解码不区分大小写；
 * 
 * @version 2015-02-03
 * @author iPan
 */
public final class HexCodec {

	static final char[] HEX = "0123456789ABCDEF".toCharArray();

    private HexCodec(){}
    
    /**
	 * This Function only pack HEX number. For example: '0','A'.
	 * input:pbyInBuffer is "1234567FE" and iInBuffLen=9; 
	 * out: pbyOutBuffer is 0x12 0x34 0x56 0x7F 0xE0.
	 */
	public static String encPack(byte[] pbyInBuffer) {
		byte n;
		int iLen, i;
		byte[] pbyOutBuffer;
		int iInBuffLen = pbyInBuffer.length;

		/*
		 * whether InBuffer data's len can be divided by 2
		 */
		if (iInBuffLen % 2 != 0)
			iLen = iInBuffLen + 1;
		else
			iLen = iInBuffLen;
		pbyOutBuffer = new byte[iLen / 2];

		for (i = 0; i < iInBuffLen; i++) {
			int curVal = pbyInBuffer[i] & 0xff; // 有符号byte转无符号byte(使用int)
			pbyInBuffer[i] = (byte) Character.toUpperCase(curVal);

			if (i % 2 != 0) { // 奇数
				n = (byte) (pbyInBuffer[i] - 0x30);

				/*
				 * Note: 'A' = 65, 'F'= 70. 65-48 = 17, 17-7=10. For example,
				 * this will convert 'A' to value 10.
				 */
				if (n > 9)
					n = (byte) (n - 7);
				pbyOutBuffer[i / 2] = (byte) (pbyOutBuffer[i / 2] | n);
			} else { // 偶数
				n = (byte) (pbyInBuffer[i] - 0x30);
				pbyOutBuffer[i / 2] = (n > 9 ? (byte) (n - 7) : n);
				pbyOutBuffer[i / 2] = (byte) (pbyOutBuffer[i / 2] << 4);
			}
		}

		// 使用ISO-8859-1编码，一个byte对应一个char。
		String result = null;
		try {
			result = new String(pbyOutBuffer, "iso-8859-1");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	/**
	 * This function unpack hex to ascii. 
	 * for example, input pbyInBuffer is 0x12 0x34 0x56 0x7F 0xE0, iInBufLen=5; 
	 * pbyOutBuffer is "1234567fe0".
	 */
	public static byte[] encUnpack(String pbyInStr) {
		int i, j;
		byte[] pbyInBuffer = null;
		try {
			pbyInBuffer = pbyInStr.getBytes("iso-8859-1");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		byte[] pbyOutBuffer = new byte[pbyInBuffer.length * 2];

		for (i = 0, j = 0; j < pbyInBuffer.length; j++, i = i + 2) {
			int curVal = pbyInBuffer[j] & 0xff; // 有符号byte转无符号byte(使用int)
			if ((curVal / 16) > 9)
				pbyOutBuffer[i] = (byte) ((curVal / 16) + '0' + 7);
			else
				pbyOutBuffer[i] = (byte) ((curVal / 16) + '0');

			if ((curVal % 16) > 9)
				pbyOutBuffer[i + 1] = (byte) ((curVal % 16) + '0' + 7);
			else
				pbyOutBuffer[i + 1] = (byte) ((curVal % 16) + '0');
		}

		return pbyOutBuffer;
	}

	public static String hexEncode2(byte[] buffer, int start, int length) {
		if (buffer.length == 0) {
			return "";
		}
		int holder = 0;
		char[] chars = new char[length * 2];
        int pos = -1;
		for (int i = start; i < start+length; i++) {
			holder = (buffer[i] & 0xf0) >> 4;
			chars[++pos * 2] = HEX[holder];
			holder = buffer[i] & 0x0f;
			chars[(pos * 2) + 1] = HEX[holder];
		}
		return new String(chars);
	}

	public static byte[] hexDecode2(String hex) {
		//A null string returns an empty array
		if (hex == null || hex.length() == 0) {
			return new byte[0];
		} else if (hex.length() < 3) {
			return new byte[]{ (byte)(Integer.parseInt(hex, 16) & 0xff) };
		}
		//Adjust accordingly for odd-length strings
		int count = hex.length();
		int nibble = 0;
		if (count % 2 != 0) {
			count++;
			nibble = 1;
		}
		byte[] buf = new byte[count / 2];
		char c = 0;
		int holder = 0;
		int pos = 0;
		for (int i = 0; i < buf.length; i++) {
		    for (int z = 0; z < 2 && pos<hex.length(); z++) {
		        c = hex.charAt(pos++);
		        if (c >= 'A' && c <= 'F') {
		            c -= 55;
		        } else if (c >= '0' && c <= '9') {
		            c -= 48;
		        } else if (c >= 'a' && c <= 'f') {
		            c -= 87;
		        }
		        if (nibble == 0) {
		            holder = c << 4;
		        } else {
		            holder |= c;
		            buf[i] = (byte)holder;
		        }
		        nibble = 1 - nibble;
		    }
		}
		return buf;
	}
	
	public static void main(String[] args) throws Exception {
		String s = "123abc";
		String s2 = encPack(s.getBytes());
		byte[] buf = encUnpack(s2);
		for (byte b : buf) {
			System.out.println(Integer.toHexString(b & 0xff));
		}
		
//		// 如何能将String与byte[]拼接后输出，保证byte[]正确输出？
//		// 问题是byte[]转换为String后输出就不对了！
//		byte[] data = {18, 58, -68}; // 0x12, 0x3a, 0xbc
//		String s = new String(data, "iso-8859-1");
//		System.out.println("len=" + s.length()); // 3byte.
//		System.out.println("len=" + s.getBytes("iso-8859-1").length); // 5byte.
//		PrintStream print = new PrintStream(new FileOutputStream("test.out"));
//		print.write(s.getBytes("iso-8859-1"));
//		print.close(); // 文件的字节也是5byte.
		
//		System.out.println("============");
//		byte[] result = encUnpack(s2);
//		for (byte b : result) {
//			System.out.print(Integer.toHexString(b & 0xff) + ", ");
//		}
//		System.out.println(new String(result));
		
//		byte a = -68;
//		byte b = 63;
//		System.out.println(Integer.toBinaryString(a & 0xff));
//		System.out.println(Integer.toBinaryString(b & 0xff));
	}

}

//C源程序如下：
///*
// * *   This Function only pack HEX number.For example: '0','A'. *  
// * input:pbyInBuffer is "1234567FE" and  iInBuffLen=9; *   out: pbyOutBuffer
// * is 0x12 0x34 0x56 0x7F 0xE0 
// */
//_INT    EncPack(_UCHR * pbyInBuffer, _UCHR * pbyOutBuffer, _INT iInBuffLen)
//{
//	_UCHR   n;
//	_INT    iLen, i;
//
//	/*
//	 * *  whether InBuffer data's len can be divided by 2 
//	 */
//	if (iInBuffLen % 2)
//		iLen = iInBuffLen + 1;
//	else
//		iLen = iInBuffLen;
//
//	for (i = 0; i < iInBuffLen; i++)
//	{
//		pbyInBuffer[i] = (_UCHR) toupper(pbyInBuffer[i]);
//
//		if (i % 2 != 0)	/* if odd number */
//		{
//			n = (_UCHR) (pbyInBuffer[i] - 0x30);
//
//			/*
//			 * *  Note: 'A' = 65, 'F'= 70.  65-48 = 17, 17-7=10. * 
//			 * For example, this will convert 'A' to value 10. 
//			 */
//			if (n > 9)
//				n = n - 7;
//			pbyOutBuffer[i / 2] = pbyOutBuffer[i / 2] | n;
//		} else
//		{
//			pbyOutBuffer[i / 2] =
//				((n = pbyInBuffer[i] - 0x30) > 9 ? n - 7 : n) << 4;
//		}
//	}
//
//	return (iLen / 2);
//
//}
//
///*
// * *   This function unpack hex to ascii . for example, *   input pbyInBuffer
// * is 0x12 0x34 0x56 0x7F 0xE0 ,iInBufLen=5; *   pbyOutBuffer is
// * "1234567fe0". 
// */PinCovProcess
//_INT    EncUnpack(_UCHR * pbyInBuffer, _UCHR * pbyOutBuffer, _INT iInBufLen)
//{
//	_INT    i, j;
//
//	for (i = 0, j = 0; j < iInBufLen; j++, i = i + 2)
//	{
//		if ((pbyInBuffer[j] / 16) > 9)
//			pbyOutBuffer[i] = (pbyInBuffer[j] / 16) + '0' + 7;
//		else
//			pbyOutBuffer[i] = (pbyInBuffer[j] / 16) + '0';
//
//		if ((pbyInBuffer[j] % 16) > 9)
//			pbyOutBuffer[i + 1] = (pbyInBuffer[j] % 16) + '0' + 7;
//		else
//			pbyOutBuffer[i + 1] = (pbyInBuffer[j] % 16) + '0';
//	}
//
//	return (0);
//
//}
