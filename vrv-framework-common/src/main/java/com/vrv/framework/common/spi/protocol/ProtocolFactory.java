package com.vrv.framework.common.spi.protocol;

import com.vrv.framework.common.spi.BaseSpi;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TTransport;

/**
 * @author chenlong
 * @date 2021/8/31 16:22
 */
public interface ProtocolFactory extends BaseSpi {

    /**
     * client 使用的协议
     *
     * @param tTransport TTransport
     * @return TProtocol
     */
    TProtocol clientProtocol(TTransport tTransport);

    /**
     * server 使用的协议工厂
     *
     * @return TProtocolFactory
     */
    TProtocolFactory serverProtocolFactory();
}
