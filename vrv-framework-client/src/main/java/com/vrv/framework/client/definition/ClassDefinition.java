package com.vrv.framework.client.definition;

import java.lang.reflect.Constructor;

import com.vrv.framework.client.annotation.VoaService;
import org.apache.thrift.protocol.TProtocol;


/**
 * XOA Service的标准定义，包括serviceId,version及thrift客户端 唯一对应发布的服务
 *
 * @author chenlong
 */
public class ClassDefinition {

    private String serviceId;

    private String version;

    private Class<?> serviceClientClass;

    private Constructor<?> serviceClientConstructor;

    public ClassDefinition(Class<?> serviceInterface) throws ClassNotFoundException, SecurityException, NoSuchMethodException {

        String clientClassName = resolveClientClassName(serviceInterface);
        this.serviceClientClass = Class.forName(clientClassName);
        revolveServiceId(serviceInterface);
        this.serviceClientConstructor = serviceClientClass.getConstructor(TProtocol.class);
    }

    /**
     * 通过解析thrift code获取客户端
     *
     * @param serviceClass
     * @return
     */
    private String resolveClientClassName(Class<?> serviceClass) {

        String packageName = serviceClass.getPackage().getName();
        // String simpleClassName = serviceClass.getSimpleName();
        // simpleClassName = simpleClassName.substring(1); // remove heading I
        // return packageName + "." + simpleClassName+"$Client";
        Class<?>[] serviceInterfaces = serviceClass.getInterfaces();
        for (int i = 0; i < serviceInterfaces.length; i++) {
            Class<?> serviceInterface = serviceInterfaces[i];
            String name = serviceInterface.getName();
            if (name.endsWith("$Iface")) {
                return name.replaceAll("\\$Iface", "\\$Client");
            }
        }
        return null;
    }

    /**
     * 获取注解XoaService的Meta Data
     *
     * @param serviceClass
     */
    private void revolveServiceId(Class<?> serviceClass) {

        VoaService xoaService = serviceClass.getAnnotation(VoaService.class);
        this.serviceId = xoaService != null ? xoaService.value().trim() : "";
        this.version = xoaService != null ? xoaService.version().trim() : "";
    }

    public String getServiceId() {

        return serviceId;
    }

    public String getVersion() {

        return version;
    }

    public Class<?> getServiceClientClass() {

        return serviceClientClass;
    }

    public Constructor<?> getServiceClientConstructor() {

        return serviceClientConstructor;
    }

    public void setServiceId(String serviceId) {

        this.serviceId = serviceId;
    }

    public void setVersion(String version) {

        this.version = version;
    }

}
