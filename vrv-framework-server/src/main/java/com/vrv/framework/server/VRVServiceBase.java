package com.vrv.framework.server;

import com.vrv.framework.common.thrift.BizMethodInfo;
import com.vrv.framework.common.thrift.BizMethodInvokeInfo;
import com.vrv.framework.common.thrift.VRVService;
import org.apache.thrift.TException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * @author chenlong
 * @date 2021/9/1 10:49
 */
public class VRVServiceBase implements VRVService.Iface {
    private VrvServer server;
    private VRVServiceInfo serviceInfo;

    @Override
    public String getName() throws TException {
        return this.server.getServerInfo().getName();
    }

    @Override
    public String getVersion() throws TException {
        return this.server.getServerInfo().getVersion();
    }

    @Override
    public List<BizMethodInfo> getServiceBizMethods() throws TException {
        if (this.serviceInfo == null) {
            return null;
        }
        return this.serviceInfo.getBizMethodInfoList();
    }

    @Override
    public Map<String, BizMethodInvokeInfo> getBizMethodsInvokeInfo() throws TException {
        if (this.serviceInfo == null) {
            return null;
        }
        return this.serviceInfo.getBizMethodInvokeInfoMap();
    }

    @Override
    public BizMethodInvokeInfo getBizMethodInvokeInfo(String methodName) throws TException {
        if (this.serviceInfo == null) {
            return null;
        }
        return this.serviceInfo.getBizMethodInvokeInfoMap().get(methodName);
    }

    @Override
    public long getServiceCount() throws TException {
        if (this.serviceInfo == null) {
            return 0;
        }
        return this.serviceInfo.getServiceCount();
    }

    @Override
    public long aliveSince() throws TException {
        // 格式化
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime parse = LocalDateTime.parse(this.server.getServerInfo().getStartTime(), dateTimeFormatter);
        // 服务启动时间戳
        long startTimeMillis = LocalDateTime.from(parse).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return (System.currentTimeMillis() / 1000) - (startTimeMillis / 1000);
    }

    @Override
    public void reinitialize() {
        this.server.reinitialize();
    }

    @Override
    public void shutdown() {
        this.server.stop();
    }

    @Override
    public void setOption(String key, String value) {
        this.server.getServerInfo().setOption(key, value);
    }

    @Override
    public Map<String, String> getOptions() {
        return this.server.getServerInfo().getOptions();
    }

    public VRVServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    public void setServiceInfo(VRVServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    public VrvServer getServer() {
        return server;
    }

    public void setServer(VrvServer server) {
        this.server = server;
    }

    @Deprecated
    public long incrementCounter(String key) throws TException {
        // donoting
        if (this.getBizMethodInvokeInfo(key) == null) {
            return 0;
        }
        return this.getBizMethodInvokeInfo(key).getTotalCount();
    }
}
