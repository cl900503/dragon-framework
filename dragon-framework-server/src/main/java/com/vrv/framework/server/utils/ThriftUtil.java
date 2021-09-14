package com.vrv.framework.server.utils;

import com.vrv.framework.common.exception.VrvTProcessorException;
import com.vrv.framework.server.proxy.Vrv;
import com.vrv.framework.server.vrv.VrvServer;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ThriftUtil {

    /**
     * @param server
     * @param service
     * @return
     * @throws VrvTProcessorException
     */
    public static TProcessor createTProcessor(VrvServer server, Object service) throws VrvTProcessorException {

        if (service == null) {
            throw new VrvTProcessorException("service 不能为空！");
        }

        // 如果是代理对象则获取目标对象
        Object targetObject = getTargetObject(service);
        // 根据业务实现类获取IfaceClass
        Class<?> ifaceClass = getIfaceClass(targetObject.getClass());
        if (ifaceClass == null) {
            throw new VrvTProcessorException(service.getClass() + "业务实现类的Iface接口找不到，是否实现了Iface接口？");
        }
        // 根据IfaceClass获取TProcessorClass
        Class<? extends TProcessor> processorClass = getTProcessorClass(ifaceClass);
        TProcessor processor = null;
        try {
            Constructor<? extends TProcessor> constructor = processorClass.getConstructor(ifaceClass);
            processor = constructor.newInstance(new Vrv()
                    .wrapper(server, targetObject, service));
        } catch (Exception e) {
            throw new VrvTProcessorException("创建业务处理器异常：" + e.getMessage());
        }
        return processor;
    }

    /**
     * 获取TProcessor类
     *
     * @param ifaceClass
     * @return TProcessorClass
     * @throws VrvTProcessorException
     */
    private static Class<? extends TProcessor> getTProcessorClass(Class<?> ifaceClass) throws VrvTProcessorException {
        String name = ifaceClass.getName();
        String processorStr = name.replaceAll("\\$Iface", "\\$Processor");
        Class<? extends TProcessor> processorClass = null;
        try {
            processorClass = (Class<? extends TProcessor>) Class.forName(processorStr);
        } catch (ClassNotFoundException e) {
            throw new VrvTProcessorException("创建不了业务处理器，找不到类！");
        }
        return processorClass;
    }

    /**
     * 根据serviceClass找出IfaceClass
     *
     * @param serviceClass
     * @return IfaceClass
     */
    private static Class<?> getIfaceClass(Class<?> serviceClass) {
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

    /**
     * 如果是代理对象获取目标对象
     *
     * @param beanInstance
     * @return
     */
    public static Object getTargetObject(Object beanInstance) throws VrvTProcessorException {
        try {
            //判断对象是否为 Spring 生成的代理对象。
            if (!AopUtils.isAopProxy(beanInstance)) {
                return beanInstance;
            } else if (AopUtils.isCglibProxy(beanInstance)) {
                //判断对象是否为 Spring 基于 CGLIB 生成的代理对象。
                return getCglibProxyTargetObject(beanInstance);
            } else if (AopUtils.isJdkDynamicProxy(beanInstance)) {
                //判断对象是否为 Spring 基于动态代理生成的代理对象。
                return getJdkDynamicProxyTargetObject(beanInstance);
            }
        } catch (Exception e) {
            throw new VrvTProcessorException("获取目标对象失败：" + e.getMessage());
        }
        return null;
    }

    private static Object getCglibProxyTargetObject(Object proxy) throws Exception {
        Field h = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
        h.setAccessible(true);
        Object dynamicAdvisedInterceptor = h.get(proxy);
        Field advised = dynamicAdvisedInterceptor.getClass().getDeclaredField("advised");
        advised.setAccessible(true);
        Object target = ((AdvisedSupport) advised.get(dynamicAdvisedInterceptor)).getTargetSource().getTarget();
        return target;
    }

    private static Object getJdkDynamicProxyTargetObject(Object proxy) throws Exception {
        Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
        h.setAccessible(true);
        AopProxy aopProxy = (AopProxy) h.get(proxy);
        Field advised = aopProxy.getClass().getDeclaredField("advised");
        advised.setAccessible(true);
        Object target = ((AdvisedSupport) advised.get(aopProxy)).getTargetSource().getTarget();
        return target;
    }


}
