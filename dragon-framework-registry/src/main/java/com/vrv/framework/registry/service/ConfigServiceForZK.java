package com.vrv.framework.registry.service;

import com.vrv.framework.common.utils.json.JacksonUtil;
import com.vrv.framework.registry.factory.ZookeeperFactory;
import com.vrv.framework.registry.model.ServiceConfigBean;
import com.vrv.framework.registry.utils.zk.ZookeeperHelp;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.thrift.TException;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author chenlong
 */
public class ConfigServiceForZK implements Register {

	private static final Logger logger = LoggerFactory.getLogger(ConfigServiceForZK.class);

	static Map<String, PathChildrenCache> pathChildrenCacheMap = new HashMap<String, PathChildrenCache>();

	public static CuratorFramework getCuratorFramework() throws Exception {

		return ZookeeperFactory.getObject();
	}

	private static String getPath(String serviceID, String version) {

		String parentPath = ZookeeperHelp.getNamespace() + "/" + serviceID + "/" + version;
		return parentPath;
	}

	@Override
	public List<ServiceConfigBean> loadServices() throws TException {

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ServiceConfigBean> queryService(String serviceID, String version) throws TException {

		List<ServiceConfigBean> configBeans = new ArrayList<ServiceConfigBean>();
		try {

			final String parentPath = getPath(serviceID, version);

			List<String> nodePaths = getCuratorFramework().getChildren().forPath(parentPath);
			if (nodePaths != null) {
				for (String nodePath : nodePaths) {
					String path = parentPath + "/" + nodePath;
					byte[] bytes = getCuratorFramework().getData().forPath(path);
					if (bytes != null) {
						ServiceConfigBean configBean = getByData(bytes);
						configBeans.add(configBean);
					}
				}
			}

		} catch (Exception e) {
			logger.error("queryService error", e);
		}

		return configBeans;
	}

	@Override
	public boolean registerService(ServiceConfigBean configBean) throws TException {

		logger.info("register service to zookeeper ServiceConfigBean:{}", configBean);

		try {
			String name = configBean.getName();
			String version = configBean.getVersion();
			String ip = configBean.getIp();
			int port = configBean.getPort();
			String SSID = configBean.getSsid();
			String address = ip + ":" + port + ":" + SSID;
			final String parentPath = getPath(name, version);
			final String path = parentPath + "/" + address;
			final byte[] bytes = JacksonUtil.toJSONString(configBean).getBytes();

			TreeCache treeCache = new TreeCache(getCuratorFramework(), path);

			bindRegisterListener(treeCache, path, bytes);
			//logger.info("register service to zookeeper start... ");
			logger.debug("register service to zookeeper start... info:{}", new String(bytes));
			registerPathData(path, bytes);

			logger.info("register service to zookeeper end. ");
			logger.info("add ShutdownHook");
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				try {
					logger.info("我要删除了");
					treeCache.close();
					getCuratorFramework().delete().deletingChildrenIfNeeded().forPath(path);
					logger.info("delete over");
				} catch (Exception e) {
//					logger.error("",e);
				}
			}));
			return true;
		} catch (Exception e) {
			// e.printStackTrace();
			logger.error("register service to zookeeper error：" + e);
		}
		return false;
	}

	public void bindRegisterListener(TreeCache treeCache, final String path, final byte[] bytes) throws Exception {

		treeCache.getListenable().addListener(((client, event) -> {
			switch (event.getType()) {
			case CONNECTION_LOST:
				logger.debug("Check Connection State LOST to re-register for path " + path);
				registerPathData(path, bytes);
				break;
			case NODE_REMOVED:
				if (event.getData().getPath().equals(path)) {
					// 检查到删除节点 重新注册
					logger.debug("Check to delete the node to re-register for path " + path);
					registerPathData(path, bytes);
				}
				break;
			default:
				break;
			}
		}));
		treeCache.start();

	}

	private void registerPathData(String path, byte[] bytes) throws Exception {

		getCuratorFramework().create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path, bytes);

	}

	private ServiceConfigBean getByData(byte[] bytes) {

		if (bytes != null) {
			String infojson = new String(bytes);
//			JSONObject object = JSONObject.parseObject(infojson);
//			ServiceConfigBean configBean = new ServiceConfigBean();
//			configBean.setTactics(object.getString("tactics"));
//			configBean.setServerID(object.getString("serverID"));
//			configBean.setDescription(object.getString("description"));
//			configBean.setIp(object.getString("ip"));
//			configBean.setPort(object.getInteger("port"));
//			configBean.setSSID(object.getString("SSID"));
//			configBean.setServiceID(object.getString("serviceID"));
//			configBean.setVersion(object.getString("version"));
////			configBean.setInstallTime(object.getLong("installTime"));
////			configBean.setInvalidTime(object.getLong("invalidTime"));
//			configBean.setLocation(object.getString("location"));
//			configBean.setName(object.getString("name"));
//			configBean.setOrgID(object.getString("orgID"));
//			configBean.setProperty(object.getByteValue("property"));

			ServiceConfigBean configBean = JacksonUtil.parseObject(infojson, ServiceConfigBean.class);

			return configBean;
		}
		return null;
	}

}
