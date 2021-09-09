package com.vrv.framework.common.intercept;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

public class VrvClinetInterceptHelp {
	private static List<VrvClientInterceptHandle> interceptHandles = new ArrayList<>();

	public static void addIntercept(VrvClientInterceptHandle vrvClientInterceptHandle) {
		interceptHandles.add(vrvClientInterceptHandle);
		// 根据order进行排序
		Collections.sort(interceptHandles, Comparator.comparingInt(VrvClientInterceptHandle::getOrder).reversed());
	}

	public static List<VrvClientInterceptHandle> getInterceptHandles() {
		return interceptHandles;
	}

	static {
		// 通过java SPI机制加载所有服务端拦截器
		ServiceLoader<VrvClientInterceptHandle> service = ServiceLoader.load(VrvClientInterceptHandle.class);
		for (VrvClientInterceptHandle i : service) {
			addIntercept(i);
		}
	}

}
