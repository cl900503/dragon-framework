package com.vrv.framework.client;

import com.vrv.framework.client.model.Node;
import org.apache.thrift.transport.TTransport;

/**
 * @author chenlong
 * @date 2021/9/9 15:01
 */
public class VoaTransport {


    private TTransport transport;
    private Node node;
    private boolean disabled = false;

    public TTransport getTransport() {
        return transport;
    }

    public void setTransport(TTransport transport) {
        this.transport = transport;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

}
