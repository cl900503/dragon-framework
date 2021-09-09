package com.vrv.framework.client;

import org.apache.thrift.transport.TTransport;

/**
 * transport连接
 * @author chenlong
 * @date 2021/9/9 15:32
 */
public class TTransportConnection {

    private TTransport transport;
    private String key;

    public TTransport getTransport() {
        return transport;
    }

    public void setTransport(TTransport transport) {
        this.transport = transport;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }


}
