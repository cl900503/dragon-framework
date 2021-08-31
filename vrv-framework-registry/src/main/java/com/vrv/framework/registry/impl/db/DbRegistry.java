package com.vrv.framework.registry.impl.db;

import com.vrv.framework.registry.Registry;
import com.vrv.framework.registry.model.ServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 使用数据库作为注册中心
 *
 * @author chenlong
 * @date 2021/8/31 11:28
 */
public class DbRegistry implements Registry {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(DbRegistry.class);

    /**
     * 服务注册
     *
     * @param serviceInfo 服务信息
     */
    @Override
    public void register(ServiceInfo serviceInfo) {
        logger.debug("db 注册成功：{}", serviceInfo);
    }

    /**
     * 服务取消注册
     *
     * @param serviceInfo 服务信息
     */
    @Override
    public void unregister(ServiceInfo serviceInfo) {
        logger.debug("db 注销成功：{}", serviceInfo);
    }
}
