package com.vrv.framework.common.spi.protocol.impl;

import com.vrv.framework.common.spi.protocol.ProtocolFactory;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TTransport;

/**
 * compact
 *
 * @author chenlong
 * @date 2021-06-21 10:42:13
 */
public class CompactProtocolFactory implements ProtocolFactory {
    @Override
    public String name() {
        return "thrift.compact";
    }

    @Override
    public TProtocol clientProtocol(TTransport tTransport) {
        return new TCompactProtocol(tTransport);
    }

    @Override
    public TProtocolFactory serverProtocolFactory() {
        return new TCompactProtocol.Factory();
    }
}
