package com.vrv.framework.server.utils;

import com.vrv.framework.common.config.TomlConfig;
import com.vrv.framework.common.model.MicroConfig;
import com.vrv.framework.common.utils.CommonUtil;
import com.vrv.framework.server.model.VrvServerInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 服务模块提供的工具类
 *
 * @author chenlong
 * @date 2021/9/1 11:14
 */
@Slf4j
public class ServerUtil {

    /**
     * 加载服务信息
     *
     * @param info
     * @throws Exception
     */
    public static void loadServerInfo(VrvServerInfo info) throws Exception {

        // 获取配置实例
        TomlConfig tomlConfig = TomlConfig.getInstance();

        // 获取服务配置
        // 如果没有传递服务名字，则默认取第一个，如果传递了服务名字，则根据名字取。
        MicroConfig microConfig = new MicroConfig();

        if (StringUtils.isNotEmpty(info.getName())) {
            microConfig = tomlConfig.getMicroConfig(info.getName());
        } else {
            microConfig = tomlConfig.getMicroConfig();
        }

        log.debug("MicroConfig：{}", microConfig);
        // micro配置校验
        if (TomlConfig.checkMicroConfig(microConfig)) {
            info.setName(microConfig.getName());
            info.setVersion(microConfig.getVersion());
            info.setWeight((int) microConfig.getWeight());

            // 服务注册ip
            info.setRegisterIp(CommonUtil.getRegisterIp());

            // scheduling
            info.setCores((int) microConfig.getSchedulingInfo().getCores());
            info.setMaxReadBuffer(microConfig.getSchedulingInfo().getMaxReadBuffer());
            info.setThreadsPerCore((int) microConfig.getSchedulingInfo().getThreadsPerCore());

            // net
            // bindIp由net.address解析获得
            info.setBindIp(microConfig.getNetInfo().bindIp());
            // port由net.address解析获得
            info.setPort(microConfig.getNetInfo().port());
            info.setNetwork(microConfig.getNetInfo().getNetwork());
            info.setProtocol(microConfig.getNetInfo().getProtocol());

            // 实例id
            info.setSSID(UUID.randomUUID().toString());
        }

        // 格式化
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 当前时间
        LocalDateTime now = LocalDateTime.now();
        // 服务启动时间
        info.setStartTime(now.format(dateTimeFormatter));
    }


}
