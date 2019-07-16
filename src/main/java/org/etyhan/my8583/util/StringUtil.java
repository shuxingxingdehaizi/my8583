package org.etyhan.my8583.util;

public class StringUtil {
	public static String align(String src,int length) {
		if(src == null) {
			return null;
		}
		String result = src;
		for(int i=0;i<length-src.length();i++) {
			result += " ";
		}
		return result;
	}
	
	public static String align(String src,int length,boolean leftOrRight,String character) {
		if(src == null) {
			return null;
		}
		String result = src;
		for(int i=0;i<length-src.length();i++) {
			if(leftOrRight) {
				result = character + result;
			}else {
				result += character;
			}
		}
		return result;
	}
	
	public static String toBinaryStr(byte src) {
		return Integer.toBinaryString(src);
	}
	
	public static String toBinaryStr(byte[] src,boolean test) {
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<src.length;i++) {
			sb.append(toBinaryStr(src[i]));
			if(test) {
				sb.append("\r\n");
			}
		}
		return sb.toString();
	}
	
	public static String toBinaryStr8(byte src) {
		return align(Integer.toBinaryString(src),8,true,"0");
	}
	
	public static String toBinaryStr8(byte[] src) {
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<src.length;i++) {
			sb.append(toBinaryStr8(src[i]));
		}
		return sb.toString();
	}
	
	/*public static byte[] binstrToBinary(String src) {
		int byteLength = src.length()%8 == 0 ? src.length()/8 : src.length()/8+1;
		byte[] bs = new byte[byteLength];
		
		byte temp = 0;
		int bsIndex = 0;
		for(int i=0;i<src.length();i++) {
			
			if("1".equals(src.charAt(i)+"")) {
				temp = (byte) ((1<<i) + temp);
			}else if("0".equals(src.charAt(i)+"")) {
				;
			}else{
				throw new RuntimeException("Invalide char in BinaryString:"+src.charAt(i));
			}
			if((i != 0 && i % 7 ==0) || i == src.length()-1) {
				bs[bsIndex++] = temp ;
			}
		}
		return bs;
	}*/
	
	public static void main(String[] args) {
		System.out.println(align("abcd", 6));
		
		System.out.println(align("abcd",6, true,"0"));
		
		System.out.println(toBinaryStr8((byte)46));
		
//		byte[] bs = binstrToBinary("11000110//");
		
		
//		System.out.println(toBinaryStr8(bs));
		
//		System.out.println(toBinaryStr("a".getBytes()));
	}
}
