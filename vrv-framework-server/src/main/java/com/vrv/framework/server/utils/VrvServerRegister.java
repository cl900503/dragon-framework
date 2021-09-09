package com.vrv.framework.server.utils;

import com.vrv.framework.registry.model.ServiceConfigBean;
import com.vrv.framework.registry.service.ConfigServerFactoryUtil;
import com.vrv.framework.server.model.VrvServerInfo;
import org.apache.thrift.server.TServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 服务注册
 *
 * @author chenlong
 * @date 2021-06-08 16:25:20
 */
public class VrvServerRegister {

    static Logger logger = LoggerFactory.getLogger(VrvServerRegister.class);

    /**
     * 注册服务信息到配置服务器
     */
    public void register(final TServer server, final VrvServerInfo info, final long waitTime) {

        new Thread(() -> {

            while (true) {
                try {
                    if (waitTime != 0) {
                        Thread.sleep(waitTime);
                    }
                    if (server == null || !server.isServing()) {
                        continue;
                    }

                    // 注册服务信息进zk
                    ServiceConfigBean configBean = toServiceConfigBean(info);
                    ConfigServerFactoryUtil.getConfigService().registerService(configBean);

                    logger.info("监听配置项");
                    //ConfigurationUtilForBm.init();
                    break;
                } catch (Exception e) {
                    logger.error("register service to zookeeper error：" + e);
                    try {
                        Thread.sleep(5000);// 睡眠5秒再试
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public static ServiceConfigBean toServiceConfigBean(VrvServerInfo vrvServerInfo) {
        ServiceConfigBean serviceConfigBean = new ServiceConfigBean();
        if (vrvServerInfo != null) {
            serviceConfigBean.setName(vrvServerInfo.getName());
            serviceConfigBean.setVersion(vrvServerInfo.getVersion());
            serviceConfigBean.setIp(vrvServerInfo.getRegisterIp());
            serviceConfigBean.setPort(vrvServerInfo.getPort());
            serviceConfigBean.setNetwork(vrvServerInfo.getNetwork());
            serviceConfigBean.setProtocol(vrvServerInfo.getProtocol());
            serviceConfigBean.setWeight(vrvServerInfo.getWeight());
            serviceConfigBean.setStartTime(vrvServerInfo.getStartTime());
            serviceConfigBean.setSsid(vrvServerInfo.getSSID());
        }
        return serviceConfigBean;
    }

}
