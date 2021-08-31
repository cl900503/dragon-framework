package com.vrv.framework.registry.factory;

import com.vrv.framework.registry.Registry;
import com.vrv.framework.registry.impl.db.DbRegistry;
import com.vrv.framework.registry.impl.zk.ZkRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chenlong
 * @date 2021/8/31 11:30
 */
public class RegistryFactory {

    /**
     * 日志
     */
    private static final Logger logger = LoggerFactory.getLogger(RegistryFactory.class);

    /**
     * 各种注册中心
     */
    private static Map<String, Registry> registrys = new HashMap<String, Registry>();

    static {
        registrys.put("zk", new ZkRegistry());
        registrys.put("db", new DbRegistry());
    }

    /**
     * 获取一个注册中心
     *
     * @param name zk or db
     * @return
     */
    public Registry get(String name) {
        Registry registry = registrys.get(name);
        if (registry == null) {
            logger.error("Registry:{} is not found!");
        }
        return registry;
    }

}
