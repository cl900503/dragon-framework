package com.vrv.framework.registry;

import com.vrv.framework.registry.model.ServiceInfo;

/**
 * @author chenlong
 * @date 2021/8/31 11:23
 */
public interface Registry {

    /**
     * 服务注册
     *
     * @param serviceInfo 服务信息
     */
    void register(ServiceInfo serviceInfo);

    /**
     * 服务取消注册
     *
     * @param serviceInfo 服务信息
     */
    void unregister(ServiceInfo serviceInfo);
}
