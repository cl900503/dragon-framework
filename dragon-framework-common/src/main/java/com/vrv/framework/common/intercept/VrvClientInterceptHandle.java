package com.vrv.framework.common.intercept;

import java.lang.reflect.Method;


public interface VrvClientInterceptHandle {


    default int getOrder(){
        return 99999;
    }
    public Object intercept(Object proxy,String serviceId, Object[] args, Method method, VrvClientMethodInvoke vrvClientMethodInvoke) throws Exception;



}
