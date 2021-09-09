package com.vrv.framework.registry.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

/**
 * 服务注册信息
 * 
 * @author chenlong
 * @date 2021-06-08 16:11:56
 */
@Data
public class ServiceConfigBean {

	/**
	 * 服务名
	 */
	private String name;

	/**
	 * 服务版本
	 */
	private String version;

	/**
	 * 服务IP
	 */
	private String ip;
	/**
	 * 服务端口
	 */
	private int port;

	/**
	 * 网络协议
	 */
	private String network;

	/**
	 * 内部协议
	 */
	private String protocol;

	/**
	 * 当前服务的权重
	 */
	private int weight;

	/**
	 * 服务启动时间
	 */
	private String startTime;

	/**
	 * 服务实例ID
	 */
	@JsonIgnore
	private String ssid;

}
