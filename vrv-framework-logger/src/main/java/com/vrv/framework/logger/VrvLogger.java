package com.vrv.framework.logger;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.core.config.Configurator;

import com.vrv.im.config.TomlConfig;

/**
 * 日志配置类
 * 
 * @author chenlong
 * @date 2021-07-06 18:06:10
 */
public class VrvLogger {

	/**
	 * 服务日志保存目录
	 */
	public static final String LOG_HOME = "/data/linkdood/logs/javaserver";

	/**
	 * 日志文件前缀
	 */
	public static final String LOG_FILE_PREFIX = "server-";

	/**
	 * 初始化日志
	 * 
	 * "logPath"默认为：LOG_HOME（可以通过入参自己设置）
	 * 
	 * "logName"默认为：LOG_FILE_PREFIX + name（可以通过入参自己设置）
	 * 
	 * "SERVER_ID"默认为：server.toml中的name（可以通过入参自己设置）
	 * 
	 * 日志配置文件路径强制为server.toml中vrv.log配置的路径（不可更改）
	 * 
	 * 注：个性化通过入参设置即可，例如：<author:chenlong>，log4j2.yml中通过${sys:author}获取
	 * 
	 * @author chenlong
	 * @date 2021-07-06 18:06:18
	 * @param params 例：<"SERVER_ID":"demo">
	 */
	public static void initialize(Map<String, String> params) {

		if (null == params) {
			params = new HashMap<>();
		}

		// 获取配置
		TomlConfig tomlConfig = TomlConfig.getInstance();
		String name = tomlConfig.getMicroConfig().getName();
		String log4j2ymlPath = tomlConfig.getLog();

		// 如果没设置日志路径，则默认
		if (!params.containsKey("logPath")) {
			params.put("logPath", LOG_HOME);
		}

		// 如果没设置日志文件名，则默认为server-name
		if (!params.containsKey("logName")) {
			params.put("logName", LOG_FILE_PREFIX + name);
		}

		// 如果没有设置SERVER_ID，则默认为name
		if (!params.containsKey("SERVER_ID")) {
			params.put("SERVER_ID", name);
		}

		params.forEach((k, v) -> {
			if (k != null && v != null) {
				// ThreadContext在配置monitorInterval时，发现配置发生变化时，再去获取是获取不到的。
//					ThreadContext.put(k, v);
				System.setProperty(k, v);
			}
		});

		// springboot设置日志配置文件路径
		System.setProperty("logging.config", log4j2ymlPath);

		// 该设置为框架要求，否则自己维护logger初始化。
		// Configurator.reconfigure();
		Configurator.initialize("Log4j2", log4j2ymlPath);

	}

	/**
	 * 初始化日志（默认设置）
	 * 
	 * @author chenlong
	 * @date 2021-07-08 14:14:25
	 */
	public static void initialize() {
		initialize(null);
	}

}
