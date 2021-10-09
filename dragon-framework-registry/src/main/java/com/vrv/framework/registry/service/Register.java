package com.vrv.framework.registry.service;

import com.vrv.framework.registry.model.ServiceConfigBean;

import java.util.List;


/**
 * @author chenlong
 */
public interface Register {

    /**
     * ********************************注册服务方法***********************************
     * ******* 各服务初次启动，注册服务信息到配置服务（后续每隔1秒维持心跳）。 参数：ServiceConfigBean：服务配置信息
     * 返回：ServiceConfigResult 判断有没有该服务实例，如果没有存储之，返回服务信息的主备属性和服务唯一标识
     * 如果有踢掉原来的服务信息，再在相同的服务中决定主备属性,返回服务信息的主备属性和服务唯一标识
     *
     * @param config
     */
    public boolean registerService(ServiceConfigBean config) throws Exception;

    /**
     * ***************************提供给voa获取正常状态服务********************************
     * **** voa加载服务使用，定时每隔1秒调用一次。 参数：无 返回：无 返回服务信息列表 过滤被踢服务
     */
    public List<ServiceConfigBean> loadServices() throws Exception;

    /**
     * ***************************提供给路由服务器查询聊天服务器用******************************
     * ****** 服务查询，定时每隔1秒调用一次。 参数：无 返回：无 返回服务信息列表 过滤被踢服务
     *
     * @param serviceID
     * @param version
     */
    public List<ServiceConfigBean> queryService(String serviceID, String version) throws Exception;

}
