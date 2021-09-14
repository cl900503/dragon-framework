package com.vrv.framework.common.intercept;

public interface VrvClientMethodInvoke {
    Object invoke(Object service, Object[] args) throws Exception;
}
