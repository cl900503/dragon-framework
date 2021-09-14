package com.vrv.framework.common.spi;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import com.vrv.framework.common.spi.protocol.ProtocolFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * protocol factory provider
 * @author chenlong
 * @date 2021-06-21 10:41:44
 */
@Slf4j
public class ProtocolFactoryProvider {

    private static Map<String, ProtocolFactory> P_MAP = new HashMap<>(4);

    static {
        ServiceLoader<ProtocolFactory> serviceLoader = ServiceLoader.load(ProtocolFactory.class);

        for (ProtocolFactory protocolFactory : serviceLoader) {
            String name = protocolFactory.name();
            log.debug("add protocol factory: {}", name);
            ProtocolFactory old = P_MAP.get(name);
            if (old != null) {
                log.warn("Duplicated name: {}, old: {},new: {}", name, old, protocolFactory);
            }
            P_MAP.put(name, protocolFactory);
        }
    }

    /**
     * 根据名称获取协议工厂
     *
     * @param name 名称
     * @return 协议工厂
     */
    public static ProtocolFactory getProtocolFactory(String name) {
        ProtocolFactory protocolFactory = P_MAP.get(name);
        if (protocolFactory == null) {
            log.warn("can't find ProtocolFactory with name: {}", name);
        }
        return protocolFactory;
    }
}
