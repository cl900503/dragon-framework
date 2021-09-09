package com.vrv.framework.registry.factory;

import com.vrv.framework.registry.utils.zk.ZookeeperHelp;
import org.apache.curator.framework.CuratorFramework;

/**
 * 获取zookeeper客户端链接
 */
public class ZookeeperFactory {


	public static CuratorFramework getObject() throws Exception {


		return ZookeeperHelp.getConnect();
	}

}
