package com.vrv.framework.common.model;

import lombok.Data;

@Data
public class SchedulingInfo {

	/**
	 * 最大工作线程上限
	 */
	public static final int MAX_WORK_THREAD = 300;

	private long cores = Runtime.getRuntime().availableProcessors();
	private long threadsPerCore = 8;
	private long maxReadBuffer = 1024 * 1024;

	public long workThreads() {
		long workThreads = cores * threadsPerCore;
		return Math.min(workThreads, MAX_WORK_THREAD);
	}

}
