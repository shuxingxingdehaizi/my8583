package org.ethan.my8583;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *  这是一个中国版的8583格式标准的类，初始源代码来源于IsoType。
 * Defines the possible values types that can be used in the fields.
 * Some types required the length of the value to be specified (NUMERIC
 * and ALPHA). Other types have a fixed length, like dates and times.
 * Other types do not require a length to be specified, like LLVAR
 * and LLLVAR.
 * @author zyplanke
 */
public enum cnType {

	/** A fixed-length numeric value. It is zero-filled to the left. */
	NUMERIC(true, 0),
	/** A fixed-length alphanumeric value. It is filled with spaces to the right. */
	ALPHA(true, 0),
	/** A variable length alphanumeric value with a 2-digit header length. */
	LLVAR(false, 0),
	/** A variable length alphanumeric value with a 3-digit header length. */
	LLLVAR(false, 0),
	/** A date in format MMddHHmmss */
	DATE10(false, 10),
	/** A date in format MMdd */
	DATE4(false, 4),
	/** A date in format yyMM */
	DATE_EXP(false, 4),
	/** Time of day in format HHmmss */
	TIME(false, 6),
	/** An amount, expressed in cents with a fixed length of 12. */
	AMOUNT(false, 12),
	
	BINARY(true, 0), // 定长二进制
	LLBIN(false, 0), // 变长二进制
	LLLBIN(false, 0); // 变长二进制

	private boolean needsLen;
	private int length;

	cnType(boolean flag, int l) {
		needsLen = flag;
		length = l;
	}

	/** Returns true if the type needs a specified length. */
	public boolean needsLength() {
		return needsLen;
	}

	/** Returns the length of the type if it's always fixed, or 0 if it's variable. */
	public int getLength() {
		return length;
	}

	/** Formats a Date if the receiver is DATE10, DATE4, DATE_EXP or TIME; throws an exception
	 * otherwise. */
	public String format(Date value) {
		if (this == DATE10) {
			return new SimpleDateFormat("MMddHHmmss").format(value);
		} else if (this == DATE4) {
			return new SimpleDateFormat("MMdd").format(value);
		} else if (this == DATE_EXP) {
			return new SimpleDateFormat("yyMM").format(value);
		} else if (this == TIME) {
			return new SimpleDateFormat("HHmmss").format(value);
		}
		throw new IllegalArgumentException("Cannot format date as " + this);
	}

	/** Formats the string to the given length (length is only useful if type is ALPHA). */
	public String format(String value, int length, String encode) {
		if (this == ALPHA) {
	    	if (value == null) {
	    		value = "";
	    	}
//	        if (value.length() > length) {
//	            return value.substring(0, length);
//	        }
//	        char[] c = new char[length];
//	        System.arraycopy(value.toCharArray(), 0, c, 0, value.length());
//	        for (int i = value.length(); i < c.length; i++) {
//	            c[i] = ' ';
//	        }
//	        return new String(c);
	    	
	    	// 将字符数组改成字节数组处理
	    	try {
				byte[] c = new byte[length];
				int strLen = value.getBytes(encode).length;
				System.arraycopy(value.getBytes(encode), 0, c, 0, (strLen > length) ? length : strLen);
				for (int i = length; i > strLen; --i) {
				    c[i - 1] = 0x20; // 填空格
				}
				return new String(c, encode);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		} else if (this == LLVAR || this == LLLVAR) {
			return value;
		} else if (this == NUMERIC) {
	        char[] c = new char[length];
	        char[] x = value.toCharArray();
	        if (x.length > length) {
	        	throw new IllegalArgumentException("Numeric value is larger than intended length: " + value + " LEN " + length);
	        }
	        int lim = c.length - x.length;
	        for (int i = 0; i < lim; i++) {
	            c[i] = '0';
	        }
	        System.arraycopy(x, 0, c, lim, x.length);
	        return new String(c);
		}
		throw new IllegalArgumentException("Cannot format String as " + this);
	}

	/** Formats the integer value as a NUMERIC, an AMOUNT, or a String. */
	public String format(long value, int length, String encode) {
		if (this == NUMERIC) {
	        char[] c = new char[length];
	        char[] x = Long.toString(value).toCharArray();
	        if (x.length > length) {
	        	throw new IllegalArgumentException("Numeric value is larger than intended length: " + value + " LEN " + length);
	        }
	        int lim = c.length - x.length;
	        for (int i = 0; i < lim; i++) {
	            c[i] = '0';
	        }
	        System.arraycopy(x, 0, c, lim, x.length);
	        return new String(c);
		} else if (this == ALPHA || this == LLVAR || this == LLLVAR) {
			return format(Long.toString(value), length, encode);
		} else if (this == AMOUNT) {
			String v = Long.toString(value);
			char[] digits = new char[12];
			for (int i = 0; i < 12; i++) {
				digits[i] = '0';
			}
			//No hay decimales asi que dejamos los dos ultimos digitos como 0
			System.arraycopy(v.toCharArray(), 0, digits, 10 - v.length(), v.length());
			return new String(digits);
		}
		throw new IllegalArgumentException("Cannot format number as " + this);
	}

	/** Formats the BigDecimal as an AMOUNT, NUMERIC, or a String. */
	public String format(BigDecimal value, int length, String encode) {
		if (this == AMOUNT) {
			String v = new DecimalFormat("0000000000.00").format(value);
			return v.substring(0, 10) + v.substring(11);
		} else if (this == NUMERIC) {
			return format(value.longValue(), length, encode);
		} else if (this == ALPHA || this == LLVAR || this == LLLVAR) {
			return format(value.toString(), length, encode);
		}
		throw new IllegalArgumentException("Cannot format BigDecimal as " + this);
	}

}
