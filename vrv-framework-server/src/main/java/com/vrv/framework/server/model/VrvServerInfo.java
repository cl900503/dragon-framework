package com.vrv.framework.server.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务信息
 *
 * @author chenlong
 * @date 2021/9/1 10:03
 */
@Data
public class VrvServerInfo {


    /**
     * 服务名
     */
    private String name;

    /**
     * 服务版本
     */
    private String version;

    /**
     * 当前服务的权重
     */
    private int weight;

    /**
     * CPU核数
     */
    private int cores;

    /**
     * 最大buffer读取大小
     */
    private long maxReadBuffer;

    /**
     * 每核线程数
     */
    private int threadsPerCore;

    /**
     * 网络协议
     */
    private String network;

    /**
     * 内部协议 binary：TBinaryProtocol compact: TCompactProtocol
     */
    private String protocol;

    /**
     * 注册IP
     */
    private String registerIp;

    /**
     * 监听IP
     */
    private String bindIp;

    /**
     * 注册/监听端口
     */
    private int port;

    /**
     * 服务启动时间
     */
    private String startTime;

    /**
     * 服务实例ID
     */
    private String SSID;

    /**
     * 设置参数项
     */
    public Map<String, String> options = new HashMap<String, String>();

    /**
     * 在已有设置上单独配置某个设置项
     *
     * @param key
     * @param value
     */
    public void setOption(String key, String value) {
        this.options.put(key, value);
    }

    /**
     * 最大WORKER线程上限
     */
    public static final int MAX_WORKER_COUNT = 280;

    /**
     * 最大SELECTOR线程上限
     */
    public static final int MAX_SELECTOR_COUNT = 20;

    /**
     * 获取WORKER线程数
     *
     * @return
     * @author chenlong
     * @date 2021-07-14 16:51:30
     */
    public int workerCount() {
        int workerCount = cores * threadsPerCore;
        return Math.min(workerCount, MAX_WORKER_COUNT);
    }

    /**
     * 获取SELECTOR线程数
     *
     * @return
     * @author chenlong
     * @date 2021-07-20 16:57:23
     */
    public int selectorCount() {
        int selectorCount = cores * 1;
        return Math.min(selectorCount, MAX_SELECTOR_COUNT);
    }


}
