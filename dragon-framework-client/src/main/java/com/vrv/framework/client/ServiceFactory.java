package com.vrv.framework.client;

import com.vrv.framework.client.filter.NodeFilter;
import com.vrv.framework.client.service.IServiceFactory;
import com.vrv.framework.client.service.impl.DefaultServiceFactory;

/**
 * 一个静态工厂类（工具类），根据接口生成代理服务类。 使用IServiceFactory的默认实现创建代理服务类。
 *
 * @author chenlong
 * @date 2021/9/9 14:51
 */
public class ServiceFactory {


    /**
     * 真正的工厂类,抽象IServiceFactory，目的在于封装创建Service实例的逻辑，与静态工厂类ServiceFactory解耦。
     * <p>
     * by xun.dai@renren-inc.com
     */
    private static IServiceFactory factory = new DefaultServiceFactory();

    /**
     * 这个是Demo，使用protected是为了防止用户调用这个方法，造成无法预料的后果
     *
     * @param factory
     */
    protected static void setFactory(IServiceFactory factory) {

        ServiceFactory.factory = factory;
    }

    /**
     * 获取一个服务，根据服务class
     *
     * @param serviceClass 服务class
     * @return <T> T 一个服务
     */
    public static <T> T getService(Class<T> serviceClass) {

        return getService(serviceClass, 5000);
    }

    /**
     * 获取一个服务，根据服务class ,并可指定获取超时时间
     *
     * @param serviceClass 服务class
     * @param timeout      超时时间(ms)
     * @return <T> T 一个服务
     */
    public static <T> T getService(Class<T> serviceClass, int timeout) {

        return factory.getService(serviceClass, timeout);
    }

    /**
     * 获取一个服务，根据服务类型 ,并可指定过节点滤条件</br>
     * <p>
     * 注：在同一个服务或者应用中,调用相同的serviceClass,过滤filter要确保一致.因为后台
     * serviceClass代理是单例的,主要是为了提高效率,如果有过滤filter不一致的需求,
     * 就需要修改AbstractServiceFactory的getService每次返回新代理
     *
     * @param serviceClass 服务class
     * @param filter       服务节点过滤条件
     * @return <T> T 一个服务
     */
    public static <T> T getService(Class<T> serviceClass, NodeFilter filter) {

        return factory.getService(serviceClass, 5000, filter);
    }

    /**
     * 获取一个服务，根据服务类型 ,并可指定超时时间和过节点滤条件</br>
     * <p>
     * 注：在同一个服务或者应用中,调用相同的serviceClass,过滤filter要确保一致.因为后台
     * serviceClass代理是单例的,主要是为了提高效率,如果有过滤filter不一致的需求,
     * 就需要修改AbstractServiceFactory的getService每次返回新代理
     *
     * @param serviceClass 服务class
     * @param timeout      超时时间(ms)
     * @param filter       服务节点过滤条件
     * @return <T> T 一个服务
     */
    public static <T> T getService(Class<T> serviceClass, int timeout, NodeFilter filter) {

        return factory.getService(serviceClass, timeout, filter);
    }

    /**
     * 获取某一类服务的所有服务，根据服务class 注：CEMS产品用
     *
     * @param serviceClass 服务class
     * @return <T> T 一类服务的所有服务
     */
    public static <T> T getAllService(Class<T> serviceClass) {

        return factory.getAllService(serviceClass, 5000);
    }

    /**
     * 获取某一类服务的所有服务，根据服务class，并可指定超时时间
     *
     * @param serviceClass 服务class
     * @param timeout      超时时间(ms)
     * @return <T> T 一类服务的所有服务
     */
    public static <T> T getAllService(Class<T> serviceClass, long timeout) {

        return factory.getAllService(serviceClass, timeout);
    }

    /**
     * 获取某一类服务的所有服务，根据服务class，并可指定超时时间
     *
     * @param serviceClass 服务class
     * @param filter       服务节点过滤条件
     * @return <T> T 一类服务的所有服务
     */
    public static <T> T getAllService(Class<T> serviceClass, NodeFilter filter) {

        return factory.getAllService(serviceClass, 5000, filter);
    }

    /**
     * 获取某一类服务的所有服务，根据服务class，并可指定超时时间和过节点滤条件
     *
     * @param serviceClass 服务class
     * @param timeout      超时时间(ms)
     * @param filter       服务节点过滤条件
     * @return <T> T 一类服务的所有服务
     */
    public static <T> T getAllService(Class<T> serviceClass, long timeout, NodeFilter filter) {

        return factory.getAllService(serviceClass, timeout, filter);
    }

    /**
     * 获取一个服务，根据服务class ,并可指定获取超时时间和版本
     *
     * @param serviceClass 服务class
     * @param version      版本 不传递读取默认版本
     * @param timeout      超时时间(ms)
     * @return <T> T 一个服务
     */
    public static <T> T getService(Class<T> serviceClass, String version, int timeout) {

        return factory.getService(serviceClass, version, timeout);
    }

    /**
     * 获取一个服务，根据服务class ,并可指定获取超时时间和版本
     *
     * @param serviceClass 服务class
     * @param serviceId    服务名称
     * @param version      版本 不传递读取默认版本
     * @param timeout      超时时间(ms)
     * @return <T> T 一个服务
     */
    public static <T> T getService(Class<T> serviceClass, String serviceId, String version, int timeout) {

        return factory.getService(serviceClass, serviceId, version, timeout);
    }

    /**
     * 根据ip和端口
     *
     * @param serviceClass
     * @param ip
     * @param port
     * @param timeout
     * @param <T>
     * @return
     */
    public static <T> T getService(Class<T> serviceClass, String ip, int port, String protocol, int timeout) {

        return factory.getService(serviceClass, ip, port, protocol, timeout);
    }


    /**
     * 关注某个服务
     *
     * @param <T>
     * @param serviceClass 服务class
     * @param timeout      超时时间(ms)
     * @author chenlong
     * @date 2021-06-22 10:13:57
     */
    public static <T> void observer(Class<T> serviceClass, int timeout) {
        factory.observer(serviceClass, timeout);
    }


}
