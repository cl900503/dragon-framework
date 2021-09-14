package com.vrv.framework.registry.service;

public class ConfigServerFactoryUtil {

	public static Register getConfigService() {

		return new ConfigServiceForZK();
	}

}
