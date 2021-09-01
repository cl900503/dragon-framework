package com.vrv.framework.server;

import com.vrv.framework.server.exception.VRVTProcessorException;
import org.apache.thrift.TProcessor;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * @author chenlong
 * @date 2021/9/1 10:35
 */
public class ClassDefinition {

    /**
     * @param server
     * @param service
     * @return
     * @throws VRVTProcessorException
     */
    public static TProcessor createTProcessor(VrvServer server, Object service)
            throws VRVTProcessorException {

        if (service == null) {
            throw new VRVTProcessorException("service 不能为空！");
        }
        Object target = getTarget(service);
        Class<?> serviceIface = getServiceIfaceClass(target.getClass());
        if (serviceIface == null) {
            throw new VRVTProcessorException(service.getClass() + "业务实现类的Iface接口找不到，是否实现了Iface接口？");
        }
        Class<? extends TProcessor> processorClass = getTProcessorClass(serviceIface);
        TProcessor processor = null;
        try {
            Constructor<? extends TProcessor> constructor = processorClass.getConstructor(serviceIface);
            processor = constructor.newInstance(new VRV()
                    .wrapper(server, target, service));
        } catch (Exception e) {
            e.printStackTrace();

            throw new VRVTProcessorException("创建业务处理器异常：" + e.getMessage());
        }
        return processor;
    }

    /**
     * 根据实现类，获取服务的业务处理器
     *
     * @param serviceIface
     * @return
     * @throws VRVTProcessorException
     */
    private static Class<? extends TProcessor> getTProcessorClass(Class<?> serviceIface)
            throws VRVTProcessorException {

        String name = serviceIface.getName();
        String processorStr = name.replaceAll("\\$Iface", "\\$Processor");
        Class<? extends TProcessor> processor;
        try {
            processor = (Class<? extends TProcessor>) Class.forName(processorStr);
        } catch (ClassNotFoundException e) {
            throw new VRVTProcessorException("创建不了业务处理器，找不到类");
        }
        return processor;
    }

    /**
     * 找出服务iface类
     *
     * @param serviceClass
     * @return
     */
    private static Class<?> getServiceIfaceClass(Class<?> serviceClass) {

        Class<?>[] serviceInterfaces = serviceClass.getInterfaces();
        for (int i = 0; i < serviceInterfaces.length; i++) {
            Class<?> serviceInterface = serviceInterfaces[i];
            String name = serviceInterface.getName();
            if (name.endsWith("$Iface")) {
                return serviceInterface;
            }
        }
        return null;
    }

    public static Object getTarget(Object beanInstance) {
        if (!AopUtils.isAopProxy(beanInstance)) {
            return beanInstance;
        } else if (AopUtils.isCglibProxy(beanInstance)) {
            return getCglibProxyTargetObject(beanInstance);

        } else if (AopUtils.isJdkDynamicProxy(beanInstance)) {
            return getJdkDynamicProxyTargetObject(beanInstance);
        }
        return null;

    }

    private static Object getCglibProxyTargetObject(Object proxy) {
        try {
            Field h = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
            h.setAccessible(true);
            Object dynamicAdvisedInterceptor = h.get(proxy);

            Field advised = dynamicAdvisedInterceptor.getClass().getDeclaredField("advised");
            advised.setAccessible(true);

            Object target = ((AdvisedSupport) advised.get(dynamicAdvisedInterceptor)).getTargetSource().getTarget();
            return target;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Object getJdkDynamicProxyTargetObject(Object proxy) {
        try {
            Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
            h.setAccessible(true);
            AopProxy aopProxy = (AopProxy) h.get(proxy);
            Field advised = aopProxy.getClass().getDeclaredField("advised");
            advised.setAccessible(true);
            Object target = ((AdvisedSupport) advised.get(aopProxy)).getTargetSource().getTarget();
            return target;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
