package com.vrv.framework.server.utils;

import com.vrv.framework.registry.Registry;
import com.vrv.framework.registry.factory.RegistryFactory;
import com.vrv.framework.registry.model.ServiceInfo;
import com.vrv.framework.server.model.VrvServerInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.server.TServer;

/**
 * @author chenlong
 * @date 2021/9/1 10:33
 */
@Slf4j
public class VrvServerRegister {

    /**
     * zk注册中心
     */
    private static Registry registry= RegistryFactory.get("zookeeper");

    /**
     * 注册服务信息到配置服务器
     */
    public void register(final TServer server, final VrvServerInfo info, final long waitTime) {

        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setName("demo");
        serviceInfo.setVersion("1.0");
        registry.register(serviceInfo);

    }


}
