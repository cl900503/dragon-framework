package com.vrv.framework.model;

import lombok.Data;

@Data
public class MicroConfig {
	private String name;
	private String version;
	private long weight = 1;
	private NetInfo netInfo = new NetInfo();
	private SchedulingInfo schedulingInfo = new SchedulingInfo();
}
