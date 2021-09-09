/**
 * @(#)AbstractServiceFactory.java, 2012-11-16.
 *
 * Copyright 2012 RenRen, Inc. All rights reserved.
 */
package com.vrv.framework.client;

import com.vrv.framework.client.definition.ClassDefinition;
import com.vrv.framework.client.filter.NodeFilter;
import com.vrv.framework.client.filter.impl.AbstractNodeFilter;
import com.vrv.framework.client.invocation.CemsServiceInvocationHandler;
import com.vrv.framework.client.registry.VoaRegistry;
import com.vrv.framework.client.registry.VoaRegistryFactory;
import com.vrv.framework.client.utils.ClassUtils;
import com.vrv.framework.common.exception.VoaClassNotFoundException;
import com.vrv.framework.common.exception.VoaNoSuchMethodException;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * 抽取公共方法，如缓存服务实例，寻址路由。
 *
 * @author dewei.tan
 * @version $Id: AbstractServiceFactory.java, v 0.1 2013-3-1 下午4:18:19 dewei.tan
 *          Exp $
 */
public abstract class AbstractServiceFactory implements IServiceFactory {

    private ConcurrentMap<Class<?>, Object> serviceCache = new ConcurrentHashMap<Class<?>, Object>();
    private ConcurrentHashMap<Class<?>, String> serviceAreas = new ConcurrentHashMap<Class<?>, String>();
    private ConcurrentMap<String, Object> serviceVersionCache = new ConcurrentHashMap<String, Object>();
    private ConcurrentHashMap<String, String> serviceVersionAreas = new ConcurrentHashMap<String, String>();

    private ConcurrentMap<String, Object> serviceCacheByIpAndPort = new ConcurrentHashMap<String, Object>();

    private ServiceRouter serviceRouter;

    public AbstractServiceFactory(ServiceRouter serviceRouter) {

        if (serviceRouter == null) {
            throw new NullPointerException();
        }
        this.serviceRouter = serviceRouter;
    }

    protected ServiceRouter getServiceRouter() {

        return serviceRouter;
    }

    @Override
    public <T> T getService(Class<T> serviceInterface) {

        return getService(serviceInterface, 250);
    }

    @Override
    public <T> T getService(Class<T> serviceInterface, NodeFilter filter) {

        return getService(serviceInterface, 250, filter);
    }

    @Override
    public <T> T getService(Class<T> serviceClass, long timeout) {

        return getService(serviceClass, timeout, null);
    }

    @Override
    public <T> void observer(Class<T> serviceClass, long timeout) {

        try {

            ClassDefinition serviceDefinition = new ClassDefinition(serviceClass);
            // 提前维护好代理
            Object serviceInstance = serviceCache.get(serviceClass);
            if (serviceInstance == null) {
                InvocationHandler handler = createInvocationHandler(serviceDefinition, timeout, null);
                @SuppressWarnings("unchecked")
                T proxy = (T) Proxy.newProxyInstance(ClassUtils.getDefaultClassLoader(), new Class<?>[] { serviceClass }, handler);
                serviceCache.put(serviceClass, proxy);
            }

            // 服务名
            String name = serviceDefinition.getServiceId();
            // 服务版本
            String version = serviceDefinition.getVersion();
            VoaRegistry defaultRegistry = VoaRegistryFactory.getInstance().getDefaultRegistry();
            defaultRegistry.observer(name, version, timeout);

        } catch (ClassNotFoundException e) {
            throw new VoaClassNotFoundException(e);
        } catch (NoSuchMethodException e) {
            throw new VoaNoSuchMethodException(e);
        }

    }

    @Override
    public <T> T getService(Class<T> serviceClass, long timeout, NodeFilter filter) {

        Object serviceInstance = serviceCache.get(serviceClass);
        // 判断过滤条件，区域服务和主服务间切换
        if (filter != null && filter instanceof AbstractNodeFilter) {
            AbstractNodeFilter nf = (AbstractNodeFilter) filter;
            String serverAreaId = nf.getServerAreaId();
            String tempAreaId = serviceAreas.putIfAbsent(serviceClass, serverAreaId);
            if (tempAreaId != null) {
                if (serviceInstance != null && serverAreaId.equals(tempAreaId)) {
                    return serviceClass.cast(serviceInstance);
                }
                serviceAreas.put(serviceClass, serverAreaId);
            }
        } else if (serviceInstance != null) {
            return serviceClass.cast(serviceInstance);
        }

        try {
            ClassDefinition serviceDefinition = new ClassDefinition(serviceClass);
            // XOASEC-148
            InvocationHandler handler = createInvocationHandler(serviceDefinition, timeout, filter);

            @SuppressWarnings("unchecked")
            T proxy = (T) Proxy.newProxyInstance(ClassUtils.getDefaultClassLoader(), new Class<?>[] { serviceClass }, handler);
            serviceCache.put(serviceClass, proxy);
            return proxy;
        } catch (ClassNotFoundException e) {
            throw new VoaClassNotFoundException(e);
        } catch (NoSuchMethodException e) {
            throw new VoaNoSuchMethodException(e);
        }
    }

    /**
     * 获取所有serviceInterface服务
     *
     * @return T
     */
    @Override
    public <T> T getAllService(Class<T> serviceInterface) {

        return getAllService(serviceInterface, 250);
    }

    @Override
    public <T> T getAllService(Class<T> serviceInterface, NodeFilter filter) {

        return getAllService(serviceInterface, 250, filter);
    }

    @Override
    public <T> T getAllService(Class<T> serviceClass, long timeout) {

        return getAllService(serviceClass, timeout, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAllService(Class<T> serviceClass, long timeout, NodeFilter filter) {

        try {
            ClassDefinition serviceDefinition = new ClassDefinition(serviceClass);
            InvocationHandler handler = createCemsInvocationHandler(serviceDefinition, timeout, filter);
            T proxy = (T) Proxy.newProxyInstance(ClassUtils.getDefaultClassLoader(), new Class<?>[] { serviceClass }, handler);
            return proxy;
        } catch (Exception e) {
            throw new RuntimeException("获取所有[" + serviceClass.getName() + "]报错");
        }

    }

    protected abstract InvocationHandler createInvocationHandler(ClassDefinition serviceDefinition, long timeout, NodeFilter filter);

    protected abstract InvocationHandler createInvocationHandler(ClassDefinition serviceDefinition, String ip, int port, String protocol,
                                                                 long timeout);

    /**
     * CEMS反射
     */
    protected InvocationHandler createCemsInvocationHandler(ClassDefinition serviceDefinition, long timeout, NodeFilter filter) {

        return new CemsServiceInvocationHandler(getServiceRouter(), serviceDefinition, timeout, filter);
    };

    /**
     * 根据特定的版本获取服务
     */
    @Override
    public <T> T getService(Class<T> serviceClass, String version, long timeout) {

        return getServiceByServiceInfo(serviceClass, null, version, timeout, null);
    }

    @Override
    public <T> T getService(Class<T> serviceClass, String serviceId, String version, long timeout) {

        return getServiceByServiceInfo(serviceClass, serviceId, version, timeout, null);
    }

    @Override
    public <T> T getService(Class<T> serviceClass, String ip, int port, String protocol, int timeout) {
        return getServiceByServiceInfo(serviceClass, ip, port, protocol, timeout);
    }

    public <T> T getServiceByServiceInfo(Class<T> serviceClass, String serviceId, String version, long timeout, NodeFilter filter) {

        String key = serviceId + "_" + version;
        Object serviceInstance = serviceVersionCache.get(key);
        // 判断过滤条件，区域服务和主服务间切换
        if (filter != null && filter instanceof AbstractNodeFilter) {
            AbstractNodeFilter nf = (AbstractNodeFilter) filter;
            String serverAreaId = nf.getServerAreaId();
            String tempAreaId = serviceVersionAreas.putIfAbsent(key, serverAreaId);
            if (tempAreaId != null) {
                if (serviceInstance != null && serverAreaId.equals(tempAreaId)) {
                    return serviceClass.cast(serviceInstance);
                }
                serviceVersionAreas.put(key, serverAreaId);
            }
        } else if (serviceInstance != null) {
            return serviceClass.cast(serviceInstance);
        }

        try {
            ClassDefinition serviceDefinition = new ClassDefinition(serviceClass);
            if (!StringUtils.isEmpty(serviceId)) {
                serviceDefinition.setServiceId(serviceId);
            }
            if (!StringUtils.isEmpty(version)) {
                serviceDefinition.setVersion(version);
            }
            // XOASEC-148
            InvocationHandler handler = createInvocationHandler(serviceDefinition, timeout, filter);

            @SuppressWarnings("unchecked")
            T proxy = (T) Proxy.newProxyInstance(ClassUtils.getDefaultClassLoader(), new Class<?>[] { serviceClass }, handler);
            serviceVersionCache.put(key, proxy);
            return proxy;
        } catch (ClassNotFoundException e) {
            throw new VoaClassNotFoundException(e);
        } catch (NoSuchMethodException e) {
            throw new VoaNoSuchMethodException(e);
        }
    }

    // 如果ap以后都注册到zk上,可以用getServiceByServiceInfo的NodeFilter进行过滤zk上的节点来获取具体哪一个ap,这里没有，直接通过ip和端口创建
    public <T> T getServiceByServiceInfo(Class<T> serviceClass, String ip, int port, String protocol, long timeout) {
        if (StringUtils.isBlank(ip) || port == 0) {
            throw new IllegalArgumentException("get service by ip and port,ip is null or port = 0");
        }
        // 应针对ip,port进行缓存
        String key = serviceClass.getSimpleName() + "-" + ip + ":" + port;
        Object serviceInstance = serviceCacheByIpAndPort.get(key);
        if (serviceInstance != null) {
            return serviceClass.cast(serviceInstance);
        }
        try {
            ClassDefinition serviceDefinition = new ClassDefinition(serviceClass);
            InvocationHandler handler = createInvocationHandler(serviceDefinition, ip, port, protocol, timeout);

            @SuppressWarnings("unchecked")
            T proxy = (T) Proxy.newProxyInstance(ClassUtils.getDefaultClassLoader(), new Class<?>[] { serviceClass }, handler);
            serviceCacheByIpAndPort.put(key, proxy);
            return proxy;
        } catch (ClassNotFoundException e) {
            throw new VoaClassNotFoundException(e);
        } catch (NoSuchMethodException e) {
            throw new VoaNoSuchMethodException(e);
        }
    }

}
