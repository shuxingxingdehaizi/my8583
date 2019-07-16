package org.ethan.my8583.example;



/**
 * 网络标识类型
 * 
 * @author iPan
 * @version 2015-1-26
 */
public enum NetIdType {

	FRONT("00"), // 前置系统 
	ATM("01"); // ATM

	private NetIdType(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	private String code;
}
