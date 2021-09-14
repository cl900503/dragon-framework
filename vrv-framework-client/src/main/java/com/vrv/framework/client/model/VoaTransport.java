package com.vrv.framework.client.model;

import com.vrv.framework.client.model.Node;
import lombok.Data;
import org.apache.thrift.transport.TTransport;

/**
 * @author chenlong
 * @date 2021/9/9 15:01
 */
@Data
public class VoaTransport {

    private TTransport transport;
    private Node node;
    //private boolean disabled = false;

}
