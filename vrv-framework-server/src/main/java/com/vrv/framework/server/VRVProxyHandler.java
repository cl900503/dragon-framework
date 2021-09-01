package com.vrv.framework.server;

import com.vrv.framework.common.intercept.VrvServerInterceptHandle;
import com.vrv.framework.common.intercept.VrvServerMethodInvoke;
import com.vrv.framework.common.intercept.VsfServerInfo;
import com.vrv.framework.common.spi.Convert2StringProvider;
import com.vrv.framework.common.spi.string.Convert2String;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author chenlong
 * @date 2021/9/1 10:52
 */
public class VRVProxyHandler implements InvocationHandler {

    static Logger logger = LoggerFactory.getLogger(VRVProxyHandler.class);
    private Object service;
    private List<VrvServerInterceptHandle> vrvServerInterceptHandles;

    private Convert2String cs = Convert2StringProvider.getConvert2String();

    protected VRVProxyHandler(Object service, List<VrvServerInterceptHandle> vrvInterceptHandles) {

        this.service = service;
        this.vrvServerInterceptHandles = vrvInterceptHandles;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        VRVServiceBase fsb = (VRVServiceBase) service;
        VRVServiceInfo serviceInfo = fsb.getServiceInfo();

        long startTime = System.currentTimeMillis();
        Object result = null;
        try {
            VrvServerMethodInvoke target = method::invoke;
            for (VrvServerInterceptHandle v : vrvServerInterceptHandles) {
                VrvServerMethodInvoke finalTarget = target;
                target = (service1, args1) -> {
                    try {
                        VsfServerInfo vsfServerInfo = new VsfServerInfo();

//						vsfServerInfo.setServerId(fsb.getServer().getServerInfo().getServerID());
                        vsfServerInfo.setServerName(fsb.getName());
                        vsfServerInfo.setServerIp(fsb.getServer().getServerInfo().getRegisterIp());
                        return v.intercept(service1, args1, vsfServerInfo, method, finalTarget);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                };
            }

            // 服务端接口日志
            logger.info("call method {} start............", method.getName());

            if (cs.filterMethod(method)) {
                if (args != null) {
                    int length = args.length;
                    for (int i = 0; i < length; i++) {
                        logger.debug("method {} arg {} ==> {}", method.getName(), i, cs.convert2String(args[i]));
                    }
                }
            }

            result = target.invoke(this.service, args);

            if (cs.filterMethod(method)) {
                logger.debug("call method {} result ==> {}", method.getName(), cs.convert2String(result));
            }

        } catch (Exception e) {
            logger.error("VRVProxyHandler.invoke => {}", e.getMessage());
            if (serviceInfo.isBizMethod(method.getName())) {
                serviceInfo.updateBizMethodInvokeInfo(method.getName(), false, 0);
            }
            throw e;
        }

        if (serviceInfo.isBizMethod(method.getName())) {
            long endTime = System.currentTimeMillis();
            serviceInfo.updateBizMethodInvokeInfo(method.getName(), true, (endTime - startTime));
            logger.info("call {} cost time ==> {}", method.getName(), endTime - startTime);
        }

        return result;
    }

}
