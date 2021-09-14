package com.vrv.framework.common.utils;

import com.vrv.framework.common.config.TomlConfig;
import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;

/**
 * 公共工具类
 *
 * @author chenlong
 * @date 2021/9/1 10:14
 */
public class CommonUtil {

    /**
     * 获取服务注册ip（优先级: inet > inetEnv > 程序获取本地首个非127的IP）
     *
     * @return
     */
    public static String getRegisterIp() {

        // 读取配置
        TomlConfig tomlConfig = TomlConfig.getInstance();

        // ip
        String ip = "127.0.0.1";

        if (StringUtils.isNotEmpty(tomlConfig.getInet())) {
            // inet不为空
            ip = tomlConfig.getInet();
        } else {
            // inet为空
            String inetEnvIp = System.getenv(tomlConfig.getInetEnv());
            if (StringUtils.isNotEmpty(inetEnvIp)) {
                // inetEnv配置不为空
                ip = inetEnvIp;
            } else {
                // inetEnv为空
                try {
                    String localIp = InetAddress.getLocalHost().getHostAddress();
                    if (StringUtils.isNotEmpty(localIp)) {
                        ip = localIp;
                    } else {
                        ip = "127.0.0.1";
                    }
                } catch (Exception e) {
                    ip = "127.0.0.1";
                }
            }
        }
        return ip;
    }


}
