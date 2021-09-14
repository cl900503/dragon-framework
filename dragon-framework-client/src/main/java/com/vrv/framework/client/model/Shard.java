package com.vrv.framework.client.model;

import java.util.ArrayList;
import java.util.List;

/**
 * shard layer
 * 
 * @author Xun Dai <xun.dai@renren-inc.com>
 *
 */
public class Shard {

	/**
	 * 本Shard对象所属的Service对象
	 */
	private Service parent;

	/**
	 * shard的值，目前使用int
	 */
	private int value;

	/**
	 * 本Shard中所有的节点
	 */
	private List<Node> nodes = new ArrayList<Node>();

//	private static byte userPriority = 1;// 1：使用优先级，优先使用主机，如果有问题才使用备机 2：使用备机,3:都使用

	public Service getParent() {
		return parent;
	}

	public void setParent(Service parent) {
		this.parent = parent;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public List<Node> getNodes() {
//		if (userPriority == 1) {// 使用主机
//			List<Node> newnodes = new ArrayList<Node>();
//			for (Node node : nodes) {
//				if (node.getProperty() == 1) {
//					newnodes.add(node);
//				}
//			}
//			if (newnodes.size() == 0) {
//				return nodes;
//			}
//			return newnodes;
//		}
//		if (userPriority == 2) {// 使用备机
//			List<Node> newnodes = new ArrayList<Node>();
//			for (Node node : nodes) {
//				if (node.getProperty() == 2) {
//					newnodes.add(node);
//				}
//			}
//			return newnodes;
//		} else {
//			return nodes;
//		}
		
		return nodes;
	}

	public List<Node> getOriginNodes() {
		return this.nodes;
	}

	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}

	public Shard(Service parent, int value, List<Node> nodes) {
		super();
		this.parent = parent;
		this.value = value;
		if (nodes != null) {
			this.nodes.addAll(nodes);
		}
	}

	@Override
	public String toString() {
		return "Shard [parent=" + parent.getId() + ", value=" + value + ", nodes=" + nodes + "]";
	}

}
