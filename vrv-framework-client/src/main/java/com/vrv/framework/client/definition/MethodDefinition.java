package com.vrv.framework.client.definition;

import java.lang.reflect.Method;

/**
 * 保存方法信息，如sharding等
 *
 * @author chenlong
 */
public class MethodDefinition {

    private Method method;

    public MethodDefinition(Method method) {
        this.method = method;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

}
