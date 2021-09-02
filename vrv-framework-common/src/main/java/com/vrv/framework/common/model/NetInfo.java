package com.vrv.framework.common.model;

import lombok.Data;

/**
 * @author chenlong
 */
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

    public static void main(String[] args) {
        NetInfo netInfo = new NetInfo();
        netInfo.setAddress(":5000");
        System.out.println("ip:" + netInfo.bindIp());
        System.out.println("port:" + netInfo.port());
        //ip:
        //port:5000
    }

}
