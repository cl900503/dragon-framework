package com.vrv.framework.common.spi.string;

import com.vrv.framework.common.spi.BaseSpi;

import java.lang.reflect.Method;

/**
 * Convert2String
 *
 * @author chenlong
 * @date 2021/8/31 16:11
 */
public interface Convert2String extends BaseSpi {

    /**
     * 过滤方法
     *
     * @param method
     * @return
     */
    default boolean filterMethod(Method method) {
        return true;
    }

    /**
     * obejct2string
     *
     * @param obj
     * @return
     */
    String convert2String(Object obj);
}
