package com.vrv.framework.server.proxy;

import com.vrv.framework.common.intercept.VrvServerInterceptHelp;
import com.vrv.framework.common.thrift.BizMethodInfo;
import com.vrv.framework.common.thrift.VrvService;
import com.vrv.framework.server.monitor.VrvServiceInfo;
import com.vrv.framework.server.vrv.VrvServer;
import com.vrv.framework.server.service.impl.VrvServiceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chenlong
 * @date 2021/9/1 10:38
 */
public class Vrv {

    static Logger logger = LoggerFactory.getLogger(Vrv.class);
    private VrvServiceInfo serviceInfo = new VrvServiceInfo();
    private VrvServerInterceptHelp vrvServerInterceptHelp = new VrvServerInterceptHelp();

//	static {
//		//通过java SPI机制加载所有服务端拦截器
//		ServiceLoader<VrvServerInterceptHandle> service = ServiceLoader.load(VrvServerInterceptHandle.class);
//		for(VrvServerInterceptHandle i:service){
//			vrvServerInterceptHelp.addIntercept(i);
//		}
//	}

    /**
     * 生成代理类
     *
     * @param service
     * @return
     */
    public Object wrapper(VrvServer server, Object service, Object proxy) {


        VrvServiceBase fsb = (VrvServiceBase) service;
        fsb.setServiceInfo(serviceInfo);
        fsb.setServer(server);
        registerServiceInfo(service);
        return Proxy.newProxyInstance(
                service.getClass().getClassLoader(),
                service.getClass().getInterfaces(),
                new VrvProxyHandler(proxy, vrvServerInterceptHelp.getInterceptHandles()));
    }

    /**
     * 注册服务方法信息，主要是是业务方法，便于后面监控
     *
     * @param service
     */
    private void registerServiceInfo(Object service) {

        Class<?>[] interfaces = service.getClass().getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            Class<?> iface = interfaces[i];
            Method[] methods = iface.getMethods();
            for (Method m : methods) {
                if (isVRVServiceIfaceMethod(m)) {
                    continue;
                }
                String methodName = m.getName();
                Class<?>[] type = m.getParameterTypes();
                BizMethodInfo biz = new BizMethodInfo();
                biz.setName(methodName);
                biz.setArgsNum((byte) type.length);
                biz.setArgsType(getArgsType(type));
                if (logger.isDebugEnabled()) {
                    logger.debug("service registerServiceInfo=" + biz.toString());
                }
                serviceInfo.addServiceBizMethod(biz);
            }
        }
    }

    private List<String> getArgsType(Class<?>[] types) {

        List<String> list = new ArrayList<String>();
        for (int i = 0; i < types.length; i++) {
            list.add(types[i].getName());
        }
        return list;
    }

    private Class<VrvService.Iface> iface = VrvService.Iface.class;

    private boolean isVRVServiceIfaceMethod(Method m) {

        Method[] methods = iface.getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].equals(m)) {
                return true;
            }
        }
        return false;
    }

}
