package com.vrv.framework.common.intercept;

import java.lang.reflect.Method;

/**
 * @author chenlong
 * @date 2021/9/1 10:47
 */
public interface VrvServerInterceptHandle {


    default int getOrder() {
        return 99999;
    }

    Object intercept(Object proxy, Object[] args, VsfServerInfo vsfServerInfo, Method method, VrvServerMethodInvoke vrvServerMethodInvoke) throws Exception;


}
