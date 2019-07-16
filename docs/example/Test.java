import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.commons.lang.StringUtils;
import org.zyp.cn8583.HexCodec;
import org.zyp.cn8583.cnMessage;
import org.zyp.cn8583.cnMessageFactory;
import org.zyp.cn8583.cnSystemTraceNumGenerator;
import org.zyp.cn8583.cnType;
import org.zyp.cn8583.impl.cnSimpleSystemTraceNumGen;
import org.zyp.cn8583.parse.cnConfigParser;




public class Test {

	private static cnSystemTraceNumGenerator TRACENUM_GEN = new cnSimpleSystemTraceNumGen((int) (System.currentTimeMillis() % 1000));
	
	public static void main(String[] args) throws Exception {
//		write();
//		read();
	}
	
	private static String createIsoHeader(NetIdType type, String seqNo) {
		String tpdu = "00000";
		return tpdu + type.getCode() + seqNo + "0000";  // TPDU+网络标识+交易流水号
	}
	
	protected static cnMessage createIsoMessage(String msgType, String trscode) {
		// 创建报文
		cnMessage message = new cnMessage(msgType, 19);
		message.setBinary(false); // 对于域不使用二进制
		int trsSeqNo = TRACENUM_GEN.nextTrace();
		String trsSeqNoStr = StringUtils.leftPad(trsSeqNo + "", 8, '0');
		// 设置头部
		String headMsg = createIsoHeader(NetIdType.ATM, trsSeqNoStr);
		message.setMessageHeaderData(0, headMsg.getBytes());
		// TODO: 设置公共报文体...
		message.setValue(3, trscode, cnType.NUMERIC, 6); // 3	交易处理码	N6	M	R
		message.setValue(11, trsSeqNoStr, cnType.NUMERIC, 8); // 11	流水号	N8	M	R
		return message;
	}
	
	public static void write() throws Exception {
		cnMessage message = createIsoMessage("0200", "160300");
		message.setValue(4, "abc", cnType.ALPHA, 4); // 右侧填空格
		message.setValue(5, 123, cnType.NUMERIC, 5); // 左侧填0
		message.setValue(6, "abcdefghijklmn", cnType.LLVAR, 0); // 变长字符
		byte[] buf = new byte[3];
		buf[0] = 49; // 1
		buf[1] = 50; // 2
		buf[2] = 51; // 3
//		String encStr = HexCodec.encPack(buf); // 特殊二进制手动编码
		message.setValue(7, buf, cnType.BINARY, 3);
		FileOutputStream fout = new FileOutputStream("messagedata.out");
		message.write(fout, 2, 16);
		fout.close();
	}
	
	public static void read() throws Exception {
		byte[] buf = new byte[2];
		FileInputStream fin = new FileInputStream("messagedata.out");
		fin.read(buf);	// 读二个字节的数据（将报文长度信息读出）
		int len = byteArrayToInt(buf);
		buf = new byte[len];
		fin.read(buf);	// 从第五个字节读取len个自己的数据到buf中
		fin.close();
		
		cnMessageFactory mfact = cnConfigParser.createDefault();
		mfact.setUseBinary(false);
		cnMessage m = mfact.parseMessage(buf, mfact.getHeaderLengthAttr("0200"));	// 解析
		print(m);
		System.out.println("--- field7 value ---");
//		String v7 = (String) m.getObjectValue(7);
//		byte[] v7by = HexCodec.encUnpack(v7); // 对特殊二进制手动解码
		byte[] v7by = (byte[]) m.getObjectValue(7);
		for (byte b : v7by) {
			System.out.println(Integer.toHexString(b));
		}
	}
	
	public static int byteArrayToInt(byte[] bytes) {
		int len = bytes.length;
		int value = 0;
		// 由高位到低位
		for (int i = 0; i < len; i++) {
			int shift = (len - 1 - i) * 8;
			value += (bytes[i] & 0x000000FF) << shift;// 往高位游
		}
		return value;
	}
	
	// 输出一个报文内容
	private static void print(cnMessage m) {
		System.out.println("----------------------------------------------------- ");
		System.out.println("Message Header = [" + new String(m.getmsgHeader()) + "]");
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

//输出：
//----------------------------------------------------- 
//Message Header = [0000001000006860000]
//Message TypeID = [0200]
//FieldID: 3 <NUMERIC>	[160300]	[160300]
//FieldID: 4 <ALPHA>	[abc ]	[abc ]
//FieldID: 5 <NUMERIC>	[00123]	[00123]
//FieldID: 6 <LLVAR>	[abcdefghijklmn]	[abcdefghijklmn]
//FieldID: 7 <ALPHA>	[0]	[0]
//FieldID: 11 <NUMERIC>	[00000686]	[00000686]
//--- field7 value ---
//31
//32
//33
//30

