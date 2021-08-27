package com.vrv.framework.common.model;

import lombok.Data;

/**
 * @author chenlong
 */
@Data
public class MicroConfig {
	private String name;
	private String version;
	private long weight = 1;
	private NetInfo netInfo = new NetInfo();
	private SchedulingInfo schedulingInfo = new SchedulingInfo();
}
