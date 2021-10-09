package com.vrv.framework.registry.service;

/**
 * @author chenlong
 */
public class ConfigServerFactoryUtil {

	public static Register getConfigService() {

		return new ConfigServiceForZK();
	}

}
