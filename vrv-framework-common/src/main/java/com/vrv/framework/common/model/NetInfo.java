package com.vrv.framework.common.model;

import lombok.Data;

@Data
public class NetInfo {

	private String address;
	private String network;
	private String protocol;

	public int port() {
		String[] strings = address.split(":");
		return Integer.parseInt(strings[strings.length - 1]);
	}

	public String bindIp() {
		int index = address.lastIndexOf(":");
		if (index < 1) {
			return "";
		}
		return address.substring(0, index);
	}

}
