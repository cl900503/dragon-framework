package com.vrv.framework.common.config;

import java.net.InetAddress;

import com.vrv.framework.common.model.MicroConfig;
import org.apache.commons.lang3.StringUtils;

/**
 * 框架配置，优先获取TomlConfig配置，如果没有根据规则获取默认值
 * 
 * @author chenlong
 * @date 2021-06-07 18:48:40
 */
public class FrameworkConfig {

	/**
	 * 获取服务注册ip（优先级: inet > inetEnv > 程序获取本地首个非127的IP）
	 * 
	 * @author chenlong
	 * @date 2021-06-09 15:48:58
	 * @return
	 */
	public static String getLocalHostIP() {

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

	/**
	 * 获取zookeeper address(优先级: address > addressEnv)
	 * 
	 * @author chenlong
	 * @date 2021-06-09 17:21:27
	 * @return
	 */
	public static String getZookeeperAddress() {
		TomlConfig tomlConfig = TomlConfig.getInstance();
		if (StringUtils.isNotEmpty(tomlConfig.getZookeeperAddress())) {
			return tomlConfig.getZookeeperAddress();
		} else {
			if (StringUtils.isNotEmpty(tomlConfig.getZookeeperAddressEnv())) {
				return System.getenv(tomlConfig.getZookeeperAddressEnv());
			} else {
				return "";
			}
		}
	}

	/**
	 * 根据服务配置获取服务端口
	 * 
	 * 解析服务配置下的address，格式如：ip:port、:port、port
	 * 
	 * @param microConfig
	 * @return
	 */
	public static int getPort(MicroConfig microConfig) {
		String port = null;
		if (microConfig != null) {
			// 获取节点地址格式如：ip:port、:port、port
			String address = microConfig.getNetInfo().getAddress();
			if (StringUtils.isNotEmpty(address)) {
				if (address.indexOf(":") >= 0) {
					port = address.substring(address.lastIndexOf(":") + 1);
				} else {
					port = address;
				}
			}
		}
		if (StringUtils.isEmpty(port)) {
			return 0;
		} else {
			return Integer.valueOf(port);
		}
	}
}
