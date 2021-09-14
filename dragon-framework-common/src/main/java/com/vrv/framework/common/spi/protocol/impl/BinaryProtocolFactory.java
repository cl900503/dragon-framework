package com.vrv.framework.common.spi.protocol.impl;

import com.vrv.framework.common.spi.protocol.ProtocolFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TTransport;

/**
 * binary
 *
 * @author chenlong
 * @date 2021-06-21 10:40:32
 */
public class BinaryProtocolFactory implements ProtocolFactory {

    @Override
    public String name() {
        return "thrift.binary";
    }

    @Override
    public TProtocol clientProtocol(TTransport tTransport) {
        return new TBinaryProtocol(tTransport);
    }

    @Override
    public TProtocolFactory serverProtocolFactory() {
        return new TBinaryProtocol.Factory();
    }
}
