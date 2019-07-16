package org.ethan.my8583.example;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.math.BigDecimal;
import java.util.Date;

import org.ehan.my8583.impl.cnSimpleSystemTraceNumGen;
import org.ethan.my8583.Head;
import org.ethan.my8583.cnMessage;
import org.ethan.my8583.cnMessageFactory;
import org.ethan.my8583.cnType;
import org.ethan.my8583.parse.cnConfigParser;

/** This little example program creates a message factory out of a XML config file,
 * creates a new message, and parses a couple of message from a text file.
 * 
 * @author zyplanke
 */
public class Example {

	public static void main(String[] args) throws Exception {
//		System.out.println(Example.class.getResource("config.xml"));
		// 配置
//		cnMessageFactory mfact = cnConfigParser.createFromXMLConfigFile("config.xml");
//		cnMessageFactory mfact = cnConfigParser.createFromUrl(Example.class.getResource("config.xml"));
		cnMessageFactory mfact = cnConfigParser.createFromStream(Example.class.getResourceAsStream("/config.xml"));
		mfact.setUseCurrentDate(true);
		// 设置系统跟踪号的生成器（用于field 11）
		mfact.setSystemTraceNumberGenerator(new cnSimpleSystemTraceNumGen((int)(System.currentTimeMillis() % 100000)));
		
		//Create a new message
		cnMessage m = mfact.newMessagefromTemplate("0200");	// 根据模板创建并初始化一个报文对象
		m.setBinary(false);		// 对于域不使用二进制
		/*if(m.setMessageHeaderData(0, new String("0123456789").getBytes()) == false) {
			System.out.println("设置报文头出错。");
			System.exit(-1);
		}*/

		m.setValue(4, new BigDecimal("501.25"), cnType.AMOUNT, 0);
		m.setValue(12, new Date(), cnType.TIME, 0);
		m.setValue(15, new Date(), cnType.DATE4, 0);
		m.setValue(17, new Date(), cnType.DATE_EXP, 0);
		m.setValue(37, 12345678, cnType.NUMERIC, 12);
		m.setValue(41, "TEST-TERMINAL", cnType.ALPHA, 16);
		
		m.setHeader(mfact.createHeader(m));
		
		FileOutputStream fout = new FileOutputStream("messagedata.out");
		m.write(fout, 4, 10);	// 把报文写到文件，并在报文前，加上表示整个报文长度的四个数字字符(10进制表示)。
		fout.close();
		
		System.out.println("\n NEW MESSAGE:");
		print(m);
		
		// 下面解析一个报文串（该串存在文件中）	
		System.out.println("\n PARSE MESSAGE FROM FILE");
		FileInputStream fin = new FileInputStream("messagedata.out");
		m = mfact.parseMessage(fin, mfact.getHeaderLengthAttr("0200"));	// 解析
		print(m);
		
		
	}

	// 输出一个报文内容
	private static void print(cnMessage m) {
		System.out.println("----------------------------------------------------- ");
		System.out.println("Message Header = [" + m.getHeader().getAsStr() + "]");
		System.out.println("Message TypeID = [" +  m.getMsgTypeID() + "]");
		for (int i = 2; i < 128; i++) {
			if (m.hasField(i)) {
				System.out.println("FieldID: " + i 
									+ " <" + m.getField(i).getType() 
									+ ">\t[" + m.getObjectValue(i) 
									+ "]\t[" + m.getField(i).toString() + "]");
			}
		}
	}

}
