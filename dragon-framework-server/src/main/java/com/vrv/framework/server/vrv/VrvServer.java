package com.vrv.framework.server.vrv;

import com.vrv.framework.server.model.VrvServerInfo;
import org.apache.thrift.protocol.TProtocolFactory;

import java.util.Map;
import java.util.function.Supplier;

/**
 * 定义服务操作接口
 *
 * @author chenlong
 * @date 2021/8/31 18:02
 */
public interface VrvServer {

    /**
     * 启动服务
     *
     * @throws Exception 启动异常
     */
    void start() throws Exception;

    /**
     * 停止服务
     */
    void stop();

    /**
     * 重启服务
     */
    void reinitialize();

    /**
     * 设置加载实现 必须
     *
     * @param loadService load
     */
    void setServiceImpl(Supplier<Object> loadService);

    /**
     * 设置服务采用协议工厂
     *
     * @param proFactory proFactory
     */
    void setProFactory(TProtocolFactory proFactory);

    /**
     * 获取服务信息
     *
     * @return 服务信息
     */
    VrvServerInfo getServerInfo();

    /**
     * 设置服务名（主要用于告知读取toml中哪一个micro配置信息，用于一个包启动多个服务时）
     *
     * @param serviceName 服务名
     */
    void setServiceName(String serviceName);

    /**
     * TODO 考虑删除
     * 设置服务参数 可选
     *
     * @param params 参数
     */
    void setOptions(Map<String, String> params);

}
