package com.vrv.framework.common.intercept;

import java.lang.reflect.InvocationTargetException;

/**
 * @author chenlong
 * @date 2021/9/1 10:48
 */
public interface VrvServerMethodInvoke {

    Object invoke(Object service, Object[] args) throws InvocationTargetException, IllegalAccessException;

}
