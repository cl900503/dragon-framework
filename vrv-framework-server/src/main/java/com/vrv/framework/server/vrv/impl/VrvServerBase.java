package com.vrv.framework.server.vrv.impl;

import com.vrv.framework.common.spi.ProtocolFactoryProvider;
import com.vrv.framework.server.utils.ThriftUtil;
import com.vrv.framework.server.model.VrvServerInfo;
import com.vrv.framework.server.utils.ServerUtil;
import com.vrv.framework.server.utils.VrvServerRegister;
import com.vrv.framework.server.vrv.VrvServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TServerTransport;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author chenlong
 * @date 2021/8/31 18:03
 */
@Slf4j
public abstract class VrvServerBase implements VrvServer {

    protected VrvServerInfo info = new VrvServerInfo();
    private VrvServerRegister register = new VrvServerRegister();
    protected volatile TServer server;
    private Supplier<Object> loadService;
    private TProtocolFactory proFactory;

    public VrvServerBase() {

    }

    /**
     * 设置 运行参数， 可选
     */
    @Override
    public void setOptions(Map<String, String> params) {

        info.setOptions(params);
    }

    @Override
    public VrvServerInfo getServerInfo() {

        return info;
    }

    @Override
    public void setServiceName(String serviceName) {
        info.setName(serviceName);
    }

    @Override
    public void setServiceImpl(Supplier<Object> loadService) {

        this.loadService = loadService;
    }

    @Override
    public void setProFactory(TProtocolFactory proFactory) {

        this.proFactory = proFactory;
    }

    /**
     * 启动服务
     *
     * @throws Exception
     */
    @Override
    public void start() throws Exception {
        // 加载服务信息
        ServerUtil.loadServerInfo(info);
        log.info("start {}:{} ...", info.getName(), info.getVersion());
        TProcessor processor = ThriftUtil.createTProcessor(this, loadService.get());
        if (this.proFactory == null) {
            this.proFactory = ProtocolFactoryProvider.getProtocolFactory(info.getProtocol()).serverProtocolFactory();
        }

        doStart(processor, this.proFactory);
    }

    /**
     * 停止服务
     */
    @Override
    public void stop() {
        log.info("stop {}:{}...", info.getName(), info.getVersion());
        server.stop();
    }

    /**
     * 重启服务
     */
    @Override
    public void reinitialize() {

        stop();
        new Thread(() -> {

            while (true) {
                if (server.isServing()) {
                    // logger.info("server is running");
                    try {
                        // sleep 一下，等待服务停止
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    continue;
                }
                try {
                    start();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                break;
            }
        }).start();
    }

    protected abstract void doStart(TProcessor processor, TProtocolFactory proFactory);

    /**
     * 返回指定类型的TServerTransport
     *
     * @param transportClass
     * @return
     */
    protected TServerTransport getServerTransport(Class<? extends TServerTransport> transportClass) {
        TServerTransport serverTransport = null;
        try {
            String ip = info.getRegisterIp();
            if (StringUtils.isNotEmpty(info.getBindIp())) {
                ip = info.getBindIp();
            }
            serverTransport = transportClass.getConstructor(InetSocketAddress.class)
                    .newInstance(new InetSocketAddress(ip, info.getPort()));
        } catch (Exception e) {
            log.error("start {}:{} Exception:", info.getName(), info.getVersion(), e);
        }
        return serverTransport;
    }

    protected void register() {

        register.register(server, info, 3000);// 3秒够启动了
    }

}
