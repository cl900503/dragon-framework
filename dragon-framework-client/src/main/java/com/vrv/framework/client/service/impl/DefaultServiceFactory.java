package com.vrv.framework.client.service.impl;

import java.lang.reflect.InvocationHandler;

import com.vrv.framework.client.definition.ClassDefinition;
import com.vrv.framework.client.filter.NodeFilter;
import com.vrv.framework.client.invocation.ServiceInvocationHandler;
import com.vrv.framework.client.router.impl.CommonServiceRouter;
import com.vrv.framework.client.service.impl.AbstractServiceFactory;

/**
 * 封装创建Service实例的逻辑，缓存Service的Proxy实例，IServiceFactory的默认实现。
 *
 * @author chenlong
 */
public class DefaultServiceFactory extends AbstractServiceFactory {

    public DefaultServiceFactory() {
        super(CommonServiceRouter.getInstance());
    }

    @Override
    protected InvocationHandler createInvocationHandler(ClassDefinition serviceDefinition, long timeOut, NodeFilter filter) {
        ServiceInvocationHandler invocationHandler = new ServiceInvocationHandler(getServiceRouter(), serviceDefinition, timeOut, filter);
        return invocationHandler;
    }

    @Override
    protected InvocationHandler createInvocationHandler(ClassDefinition serviceDefinition, String ip, int port, String protocol,
                                                        long timeout) {
        return new ServiceInvocationHandler(getServiceRouter(), serviceDefinition, ip, port, protocol, timeout);
    }


}
