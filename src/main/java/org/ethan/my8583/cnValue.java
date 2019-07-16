package org.ethan.my8583;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

/**
 * 这是一个中国版的8583格式标准的类，初始源代码来自于IsoValue。
 * Represents a value that is stored in a field inside a china 8583 message.
 * It can format the value when the message is generated.
 * Some values have a fixed length, other values require a length to be specified
 * so that the value can be padded to the specified length. LLVAR and LLLVAR
 * values do not need a length specification because the length is calculated
 * from the stored value.
 * 
 * @author zyplanke
 */
public class cnValue<T> {
	private cnType datatype;
	private T value;
	private int length;
	/** 字符串编码 */
	private String encode = "UTF-8";
	
	public String getEncode() {
		return encode;
	}

	public void setEncode(String encode) {
		this.encode = encode;
	}

	/**
	 * 默认使用UTF-8编码（有一部分ASCII编码的，可以使用UTF-8）；
	 */
	public cnValue(cnType t, T value) {
		this(t, value, "UTF-8");
	}
	/** Creates a new instance that stores the specified value as the specified type.
	 * Useful for storing LLVAR or LLLVAR types, as well as fixed-length value types
	 * like DATE10, DATE4, AMOUNT, etc.
	 * @param t the cnType.
	 * @param value The value to be stored. */
	public cnValue(cnType t, T value, String encode) {
		if (t.needsLength()) {
			throw new IllegalArgumentException("Fixed-value types must use constructor that specifies length");
		}
		datatype = t;
		this.value = value;
		this.encode = encode;
		if (datatype == cnType.LLVAR || datatype == cnType.LLLVAR) {
//			length = value.toString().length(); // 修改：以具体字节数组长度为准；
			try {
				this.length = value.toString().getBytes(encode).length;
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
			if (t == cnType.LLVAR && length > 99) {
				throw new IllegalArgumentException("LLVAR can only hold values up to 99 chars");
			} else if (t == cnType.LLLVAR && length > 999) {
				throw new IllegalArgumentException("LLLVAR can only hold values up to 999 chars");
			}
		} else if (datatype == cnType.LLBIN || datatype == cnType.LLLBIN) { // 追加
			length = ((byte[])value).length;
			if (t == cnType.LLBIN && length > 99) {
				throw new IllegalArgumentException("LLBIN can only hold values up to 99 chars");
			} else if (t == cnType.LLLBIN && length > 999) {
				throw new IllegalArgumentException("LLLBIN can only hold values up to 999 chars");
			}
		} else {
			length = datatype.getLength();
		}
	}

	/**
	 * 默认使用UTF-8编码（有一部分ASCII编码的，可以使用UTF-8）；
	 */
	public cnValue(cnType t, T val, int len) {
		this(t, val, len, "UTF-8");
	}
	/** Creates a new instance that stores the specified value as the specified type.
	 * Useful for storing fixed-length value types. */
	public cnValue(cnType t, T val, int len, String encode) {
		datatype = t;
		value = val;
		length = len;
		this.encode = encode;
		if (length == 0 && t.needsLength()) {
			throw new IllegalArgumentException("Length must be greater than zero");
		} else if (t == cnType.LLVAR || t == cnType.LLLVAR) {
//			length = val.toString().length(); // 修改:以len为准;
			if (t == cnType.LLVAR && length > 99) {
				throw new IllegalArgumentException("LLVAR can only hold values up to 99 chars");
			} else if (t == cnType.LLLVAR && length > 999) {
				throw new IllegalArgumentException("LLLVAR can only hold values up to 999 chars");
			}
		} else if (t == cnType.LLBIN || t == cnType.LLLBIN) { // 追加
//			length = ((byte[])val).length; // 修改:以len为准;
			if (t == cnType.LLBIN && length > 99) {
				throw new IllegalArgumentException("LLBIN can only hold values up to 99 chars");
			} else if (t == cnType.LLLBIN && length > 999) {
				throw new IllegalArgumentException("LLLBIN can only hold values up to 999 chars");
			}
		}
	}

	/** Returns the cnType to which the value must be formatted. */
	public cnType getType() {
		return datatype;
	}

	/** Returns the length of the stored value, of the length of the formatted value
	 * in case of NUMERIC or ALPHA. It doesn't include the field length header in case
	 * of LLVAR or LLLVAR. */
	public int getLength() {
		return length;
	}

	/** Returns the stored value without any conversion or formatting. */
	public T getValue() {
		return value;
	}

	/** Returns the formatted value as a String. The formatting depends on the type of the
	 * receiver. */
	public String toString() {
		if (value == null) {
			return "FieldValue<null>";
		}
		if (datatype == cnType.NUMERIC || datatype == cnType.AMOUNT) {
			if (datatype == cnType.AMOUNT) {
				return datatype.format((BigDecimal)value, 12, encode);
			} else if (value instanceof Number) {
				return datatype.format(((Number)value).longValue(), length, encode);
			} else {
				return datatype.format(value.toString(), length, encode);
			}
		} else if (datatype == cnType.ALPHA) {
			return datatype.format(value.toString(), length, encode);
		} else if (datatype == cnType.LLLVAR || datatype == cnType.LLLVAR) {
			return value.toString();
		} else if (value instanceof Date) {
			return datatype.format((Date)value);
		} else if (datatype == cnType.BINARY || datatype == cnType.LLBIN || datatype == cnType.LLLBIN) { // 追加二进制的处理
			String binStr = null;
			try {
				binStr = new String((byte[])value, "iso-8859-1");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
			return binStr;
		}
		return value.toString();
	}

	/** Returns a copy of the receiver that references the same value object. */
	@SuppressWarnings("unchecked")
	public cnValue<T> clone() {
		return (cnValue<T>)(new cnValue(this.datatype, this.value, this.length, encode));

	}

	/** Returns true of the other object is also an cnValue and has the same type and length,
	 * and if other.getValue().equals(getValue()) returns true. */
	public boolean equals(Object other) {
		if (other == null || !(other instanceof cnValue)) {
			return false;
		}
		cnValue comp = (cnValue)other;
		return (comp.getType() == getType() && comp.getValue().equals(getValue()) && comp.getLength() == getLength());
	}

	/** Writes the formatted value to a stream, with the length header
	 * if it's a variable length type. */
	public void write(OutputStream outs, boolean binary) throws IOException { /// 输出报文域
		if (datatype == cnType.LLLVAR || datatype == cnType.LLVAR) {
			if (binary) {
				if (datatype == cnType.LLLVAR) {
					outs.write(length / 100); //00 to 09 automatically in BCD
				}
				//BCD encode the rest of the length
				outs.write((((length % 100) / 10) << 4) | (length % 10));
			} else {
				//write the length in ASCII
				if (datatype == cnType.LLLVAR) {
					outs.write((length / 100) + 48);
				}
				if (length >= 10) {
					outs.write(((length % 100) / 10) + 48);
				} else {
					outs.write(48);
				}
				outs.write((length % 10) + 48);
			}
		} else if (datatype == cnType.LLLBIN || datatype == cnType.LLBIN) {
			if (binary) {
				if (datatype == cnType.LLLBIN) {
					outs.write(length / 100); //00 to 09 automatically in BCD
				}
				//BCD encode the rest of the length
				outs.write((((length % 100) / 10) << 4) | (length % 10));
			} else {
				//write the length in ASCII
				if (datatype == cnType.LLLBIN) {
					outs.write((length / 100) + 48);
				}
				if (length >= 10) {
					outs.write(((length % 100) / 10) + 48);
				} else {
					outs.write(48);
				}
				outs.write((length % 10) + 48);
			}
		} else if (binary) {
			//numeric types in binary are coded like this
			byte[] buf = null;
			if (datatype == cnType.NUMERIC) {
				buf = new byte[(length / 2) + (length % 2)];
			} else if (datatype == cnType.AMOUNT) {
				buf = new byte[6];
			} else if (datatype == cnType.DATE10 || datatype == cnType.DATE4 || datatype == cnType.DATE_EXP || datatype == cnType.TIME) {
				buf = new byte[length / 2];
			}
			//Encode in BCD if it's one of these types
			if (buf != null) {
				toBcd(toString(), buf);
				outs.write(buf);
				return;
			}
		}
		//Just write the value as text
		if (datatype == cnType.BINARY || datatype == cnType.LLBIN || datatype == cnType.LLLBIN) { // 追加二进制的处理
			outs.write((byte[]) value);
		} else {
			outs.write(toString().getBytes(encode)); // 修改：指定字符串编码；
		}
	}

	/** Encode the value as BCD and put it in the buffer. The buffer must be big enough
	 * to store the digits in the original value (half the length of the string). */
	private void toBcd(String value, byte[] buf) {
		int charpos = 0; //char where we start
		int bufpos = 0;
		if (value.length() % 2 == 1) {
			//for odd lengths we encode just the first digit in the first byte
			buf[0] = (byte)(value.charAt(0) - 48);
			charpos = 1;
			bufpos = 1;
		}
		//encode the rest of the string
		while (charpos < value.length()) {
			buf[bufpos] = (byte)(((value.charAt(charpos) - 48) << 4)
					| (value.charAt(charpos + 1) - 48));
			charpos += 2;
			bufpos++;
		}
	}
	
	/**
	 * 字符串右侧填充空格（默认GBK编码）
	 * 
	 * 说明：针对自定义域的字符串拼接使用；
	 * 
	 * @param value 字符串（可以为null）
	 * @param length 字节长度（必须、定长）
	 * @return 字符串
	 */
	public static String strRightPad(String value, int length) {
		return strRightPad(value, length, "GBK");
	}
	/**
	 * 字符串右侧填充空格
	 * 
	 * 说明：针对自定义域的字符串拼接使用；
	 * 
	 * @param value 字符串（可以为null）
	 * @param length 字节长度（必须、定长）
	 * @param encode 编码
	 * @return 字符串
	 */
	public static String strRightPad(String value, int length, String encode) {
    	if (value == null) {
    		return StringUtils.rightPad("", length, " ");
    	}
    	// 将字符数组改成字节数组处理
    	try {
			byte[] c = new byte[length];
			byte[] valByt = value.getBytes(encode);
			int strLen = valByt.length;
			System.arraycopy(valByt, 0, c, 0, (strLen > length) ? length : strLen);
			for (int i = length; i > strLen; --i) {
			    c[i - 1] = 0x20; // 填空格
			}
			return new String(c, encode);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 从字符串中截取固定长度的字符串（默认GBK编码）
	 * 
	 * 说明：针对自定义域的字符串拼接使用；
	 * 
	 * @param str 字符串（可以为null）
	 * @param from 位置（必须）
	 * @param len 字节长度（必须）
	 * @return 字符串
	 */
	public static String strCut(String str, int from, int len) {
		return strCut(str, from, len, "GBK");
	}
	/**
	 * 从字符串中截取固定长度的字符串
	 * 
	 * 说明：针对自定义域的字符串拼接使用；
	 * 
	 * @param str 字符串（可以为null）
	 * @param from 位置（必须）
	 * @param len 字节长度（必须）
	 * @param encode 编码
	 * @return 字符串
	 */
	public static String strCut(String str, int from, int len, String encode) {
		if (str == null || str.length() < 1) {
			return str;
		}
		try {
			byte[] srcBuf = str.getBytes(encode);
			if (srcBuf.length < from + len) {
				throw new IllegalArgumentException("参数不正确.");
			}
			byte[] newBuf = new byte[len];
			for (int i=from, count=from + len; i<count; ++i) {
				newBuf[i - from] = srcBuf[i];
			}
			return new String(newBuf, encode);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("截取字符串失败.");
		}
	}

}
