package org.ethan.my8583;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.etyhan.my8583.util.StringUtil;

public class Head {
	/**
	 * Header-Length
	 * 8位二进制数，值必须为46
	 */
	private byte headerLength;
	
	/**
	 * flagAndVersion[0] = 0 表示生产报文
	 * flagAndVersion[0] = 1 表示测试报文
	 */
	private byte flagAndVersion;
	
	
	/**
	  * 域3
	 * 报文总长度
	 * 4位定长字符
	 */
	private byte[] totalMsgLength = new byte[4];
	
	/**
	 * 域4
	 * 目的ID
	 * 11位定长字母或者数字字符数据，不足后补空格
	 */
	private String destinationStationId;
	
	/**
	  * 域5
	  *  源ID
	 * 11位定长字母或者数字字符数据，不足后补空格
	 */
	private String sourceStationId;
	
	/**
	 * 域6
	 * 保留
	 * 24bit
	 */
	private byte[] reserverForUse = new byte[3];
	
	/**
	 * 域7
	 * 8bit
	 */
	private byte batchNumber;
	
	/**
	 * 域8
	 * 交易信息
	 * 8位字母/数字/特殊字符
	 */
	private byte[] transactionInformation = new byte[8];
	
	
	/**
	 * 域9
	 * 用户信息
	 * 8bit二进制
	 */
	private byte userInformation;
	
	/**
	 * 域10
	 * 拒绝码
	 * 5位定长数字字符串
	 * @return
	 */
	private byte[] rejectCode = new byte[5];
 	
	
	public byte getHeaderLength() {
		return headerLength;
	}
	
	public String getHeaderLengthInBinStr() {
		return  StringUtil.align(Integer.toBinaryString(headerLength), 8, true, "0");
	}

	public Head setHeaderLength(byte headerLength) {
		this.headerLength = headerLength;
		return this;
	}

	public byte getFlagAndVersion() {
		return flagAndVersion;
	}
	

	public Head setFlagAndVersion(byte flagAndVersion) {
		this.flagAndVersion = flagAndVersion;
		return this;
	}

	public byte[] getTotalMsgLength() {
		return totalMsgLength;
	}

	public Head setTotalMsgLength(byte[] totalMsgLength) {
		this.totalMsgLength = totalMsgLength;
		return this;
	}

	public String getDestinationStationId() {
		return destinationStationId;
	}

	public Head setDestinationStationId(String destinationStationId) {
		this.destinationStationId = StringUtil.align(destinationStationId, 11);
		return this;
	}

	public String getSourceStationId() {
		return sourceStationId;
	}

	public Head setSourceStationId(String sourceStationId) {
		this.sourceStationId = StringUtil.align(sourceStationId,11);
		return this;
	}
	
	

	public byte[] getReserverForUse() {
		return reserverForUse;
	}

	public Head setReserverForUse(byte[] reserverForUse) {
		this.reserverForUse = reserverForUse;
		return this;
	}

	public byte getBatchNumber() {
		return batchNumber;
	}

	public Head setBatchNumber(byte batchNumber) {
		this.batchNumber = batchNumber;
		return this;
	}

	public byte[] getTransactionInformation() {
		return transactionInformation;
	}

	public Head setTransactionInformation(byte[] transactionInformation) {
		this.transactionInformation = transactionInformation;
		return this;
	}

	public byte getUserInformation() {
		return userInformation;
	}

	public Head setUserInformation(byte userInformation) {
		this.userInformation = userInformation;
		return this;
	}

	public byte[] getRejectCode() {
		return rejectCode;
	}

	public Head setRejectCode(byte[] rejectCode) {
		this.rejectCode = rejectCode;
		return this;
	}
	
//	public String getAsBinStr() {
//		return this.getHeaderLengthInHexStr()+this.getFlagAndVersionInHexStr()+this.getTotalMsgLength()+getDestinationStationId()+getSourceStationId()+getReserverForUse()+getBatchNumber()+getTransactionInformation()+getUserInformation()+getRejectCode();
//	}
	
	public byte[] getAsBinary() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] temp = new byte[1];
		temp[0] = this.headerLength;
		bos.write(temp);
		
		temp = new byte[1];
		temp[0] = this.flagAndVersion;
		bos.write(temp);
		
		bos.write(this.totalMsgLength);
		
		bos.write(this.destinationStationId.getBytes());
		
		bos.write(this.sourceStationId.getBytes());
		
		bos.write(this.reserverForUse);
		
		temp = new byte[1];
		temp[0] = this.batchNumber;
		bos.write(temp);
		
		bos.write(this.transactionInformation);
		
		temp = new byte[1];
		temp[0] = this.userInformation;
		bos.write(temp);
		
		bos.write(this.rejectCode);
		
		
		byte[] resoult = bos.toByteArray();
		bos.close();
		return resoult;
	}
	
	/**
	 * 只用于日志输出
	 * @return
	 */
	public String getAsStr() {
		StringBuffer result = new StringBuffer();
		result.append("headerLength=").append(this.headerLength)
		.append(",flagAndVersion=").append(flagAndVersion)
		.append(",totalMsgLength=").append(new String(totalMsgLength))
		.append(",destinationStationId=").append(destinationStationId)
		.append(",sourceStationId=").append(sourceStationId)
		.append(",reserverForUse=").append(StringUtil.toBinaryStr(reserverForUse, false))
		.append(",batchNumber=").append(batchNumber)
		.append(",transactionInformation=").append(new String(transactionInformation))
		.append(",userInformation=").append(userInformation)
		.append(",rejectCode=").append(new String(rejectCode));
		return result.toString();
	}
	
	/**
	 * 此处截取各个字段长度后期应改为从XML配置
	 * @param headByte
	 */
	public Head(byte[] headByte) {
		if(headByte == null || headByte.length != 46) {
			throw new RuntimeException("Head length error");
		}
		this.headerLength = headByte[0];
		this.flagAndVersion = headByte[1];
		this.totalMsgLength = Arrays.copyOfRange(headByte, 2, 6);
		this.destinationStationId = new String(Arrays.copyOfRange(headByte, 6, 17));
		this.sourceStationId = new String(Arrays.copyOfRange(headByte, 17, 28));
		this.reserverForUse = Arrays.copyOfRange(headByte, 28, 31);
		this.batchNumber = headByte[31];
		this.transactionInformation = Arrays.copyOfRange(headByte, 32, 40);
		this.userInformation = headByte[40];
		this.rejectCode = Arrays.copyOfRange(headByte, 41, 46);
	}
	
	public Head() {}

	public static void main(String[] args) throws IOException {
//		byte b = 10;
//		System.out.println(StringUtil.toBinaryStr("2".getBytes(),true));
		Head h = new Head()
		.setHeaderLength((byte)46)
		.setBatchNumber((byte)120)
		.setFlagAndVersion((byte)1)
		.setRejectCode("00000".getBytes())
		.setReserverForUse(new byte[] {0,0,0})
		.setSourceStationId("00010000   ")
		.setDestinationStationId("00051400   ")
		.setTotalMsgLength("2130".getBytes())
		.setTransactionInformation("20000000".getBytes())
		.setUserInformation((byte)10);
		
		byte[] headByte = h.getAsBinary();
		System.out.println(StringUtil.toBinaryStr(headByte,true));
		
		Head h1 = new Head(headByte);
		
		System.out.println(h1.getAsStr());
		
		long ab = 999999999999999999l;
		System.out.println(ab);
		long l = 2<<120;
		System.out.println(l);
		
		System.out.println("-------------");
		System.out.println(new String(Arrays.copyOfRange(headByte, 2, 6)));
		
	}
}
