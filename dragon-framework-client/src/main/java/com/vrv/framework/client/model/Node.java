package com.vrv.framework.client.model;

/**
 * 服务节点信息
 * 
 * @author chenlong
 * @date 2021-06-09 11:34:15
 */
public class Node {

	public Node(String host, int port) {
		super();
		this.host = host;
		this.port = port;
	}

	/**
	 * host
	 */
	private String host;

	/**
	 * port
	 */
	private int port;

	/**
	 * 网络协议，如："tcp"
	 */
	private String network;

	/**
	 * 内部协议，如：thrift.binary；thrift.compact
	 */
	private String protocol;

	/**
	 * 当前服务的权重，默认1，预留字段
	 */
	private long weight;
	
	/**
	 * 当前权重，供平滑加权算法使用
	 */
	private long currentWeight;
//    /**
//     * 主机还是备份
//     */
//    private byte property;
//    /**
//     * 策略
//     */
//    private String tactics;
//    /**
//     * 区域信息
//     */
//    private String orgID;

	/**
	 * 节点是否被禁用
	 */
	private boolean disabled = true;

	/**
	 * 节点是否健康
	 */
	private boolean healthy = true;

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

//    public byte getProperty() {
//		return property;
//	}
//
//	public void setProperty(byte property) {
//		this.property = property;
//	}
//
//	public String getTactics() {
//		return tactics;
//	}
//
//	public void setTactics(String tactics) {
//		this.tactics = tactics;
//	}
//
//	public String getOrgID() {
//		return orgID;
//	}
//
//	public void setOrgID(String orgID) {
//		this.orgID = orgID;
//	}

	public int compareTo(Node o) {

		int cmp = this.host.compareTo(o.getHost());
		if (cmp != 0) {
			return cmp;
		}
		if (this.port < o.getPort()) {
			return -1;
		}
		if (this.port > o.getPort()) {
			return 1;
		}
		return 0;
	}

	public void setHealthy(boolean healthy) {
		this.healthy = healthy;
	}

	public boolean isHealthy() {
		return healthy;
	}

//	public Node(String host, int port, boolean disabled, boolean healthy) {
//		super();
//		this.host = host;
//		this.port = port;
//		this.disabled = disabled;
//		this.healthy = healthy;
////        this.property=property;
////        this.tactics=tactics;
////        this.orgID=orgID;
//	}

	public Node(String host, int port, String network, String protocol, long weight, boolean disabled, boolean healthy) {
		super();
		this.host = host;
		this.port = port;
		this.network = network;
		this.protocol = protocol;
		this.weight = weight;
		this.disabled = disabled;
		this.healthy = healthy;
	}

	public String getNetwork() {
		return network;
	}

	public void setNetwork(String network) {
		this.network = network;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public long getWeight() {
		return weight;
	}

	public void setWeight(long weight) {
		this.weight = weight;
	}

	@Override
	public String toString() {
		return "Node [host=" + host + ", port=" + port + ", network=" + network + ", protocol=" + protocol + ", weight="
				+ weight + ", currentWeight=" + currentWeight + ", disabled=" + disabled + ", healthy=" + healthy + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + port;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (getClass() != obj.getClass())) {
			return false;
		}
		Node other = (Node) obj;
		if (host == null) {
			if (other.host != null) {
				return false;
			}
		} else if (!host.equals(other.host)) {
			return false;
		}
		if (port != other.port) {
			return false;
		}
		return true;
	}

	public long getCurrentWeight() {
		return currentWeight;
	}

	public void setCurrentWeight(long currentWeight) {
		this.currentWeight = currentWeight;
	}

	/**
	 * @return
	 */
	public String getNodeKey() {
		return this.getHost() + ':' + this.getPort();
	}

}
