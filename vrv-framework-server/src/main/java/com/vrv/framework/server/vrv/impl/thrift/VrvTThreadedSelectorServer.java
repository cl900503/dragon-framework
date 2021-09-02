package com.vrv.framework.server.vrv.impl.thrift;

import com.vrv.framework.server.vrv.impl.VrvServerBase;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.layered.TFramedTransport;

/**
 * TThreadedSelectorServer
 *
 * @author chenlong
 * @date 2021/8/31 18:02
 */
@Slf4j
public class VrvTThreadedSelectorServer extends VrvServerBase {

    @Override
    protected void doStart(TProcessor processor, TProtocolFactory proFactory) {

        TNonblockingServerSocket serverTransport = (TNonblockingServerSocket) getServerTransport(
                TNonblockingServerSocket.class);
        if (serverTransport == null) {
            log.error("start {}:{} Error:create TServerTransport fail!", info.getName(), info.getVersion());
            return;
        }

        // TThreadedSelectorServer
        TThreadedSelectorServer.Args params = new TThreadedSelectorServer.Args(serverTransport);
        params.processor(processor);
        params.protocolFactory(proFactory);
        params.selectorThreads(info.selectorCount());
        params.workerThreads(info.workerCount());
        // 设置能够处理的最大参数块
        params.transportFactory(new TFramedTransport.Factory((int) info.getMaxReadBuffer()));
        // 设置读的最大参数块 默认最大long，容易引起内存溢出，必须限制
        params.maxReadBufferBytes = info.getMaxReadBuffer();
        // 使用非阻塞式IO，客户端需要指定TFramedTransport数据传输的方式
        server = new TThreadedSelectorServer(params);
        log.info("start {}:{} on port {}.", info.getName(), info.getVersion(), info.getPort());
        // 注册服务信息到注册中心
        register();
        // 启动服务
        server.serve();
    }


}
