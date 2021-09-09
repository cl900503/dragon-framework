package com.vrv.framework.client;

import com.vrv.framework.client.filter.NodeFilter;

/**
 * 服务工厂，获取实例。
 *
 * @author chenlong
 */
public interface IServiceFactory {

    public <T> T getService(Class<T> serviceInterface);

    public <T> T getService(Class<T> serviceInterface, long timeout);

    public <T> T getService(Class<T> serviceInterface, NodeFilter filter);

    public <T> T getService(Class<T> serviceInterface, long timeout, NodeFilter filter);

    public <T> T getAllService(Class<T> serviceInterface);

    public <T> T getAllService(Class<T> serviceInterface, long timeout);

    public <T> T getAllService(Class<T> serviceInterface, NodeFilter filter);

    public <T> T getAllService(Class<T> serviceInterface, long timeout, NodeFilter filter);

    public <T> T getService(Class<T> serviceClass, String version, long timeout);

    public <T> T getService(Class<T> serviceClass, String serviceId, String version, long timeout);

    public <T> T getService(Class<T> serviceClass, String ip, int port, String protocol, int timeout);

    public <T> void observer(Class<T> serviceInterface, long timeout);
}
