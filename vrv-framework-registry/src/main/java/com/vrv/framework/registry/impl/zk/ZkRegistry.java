package com.vrv.framework.registry.impl.zk;

import com.vrv.framework.registry.Registry;
import com.vrv.framework.registry.model.ServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 使用zookeeper作为注册中心
 *
 * @author chenlong
 * @date 2021/8/31 11:26
 */
public class ZkRegistry implements Registry {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(ZkRegistry.class);

    /**
     * 服务注册
     *
     * @param serviceInfo 服务信息
     */
    @Override
    public void register(ServiceInfo serviceInfo) {
        logger.debug("zookeeper 注册成功：{}", serviceInfo);
    }

    /**
     * 服务取消注册
     *
     * @param serviceInfo 服务信息
     */
    @Override
    public void unregister(ServiceInfo serviceInfo) {
        logger.debug("zookeeper 注销成功：{}", serviceInfo);
    }
}
