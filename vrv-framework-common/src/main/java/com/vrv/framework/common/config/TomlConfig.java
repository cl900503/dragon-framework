package com.vrv.framework.common.config;

import com.vrv.framework.common.model.MicroConfig;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 服务配置（读取resource下server.toml）
 *
 * @author chenlong
 * @date 2021-06-07 18:48:40
 */
@Getter
public class TomlConfig {

    private static Logger logger = LoggerFactory.getLogger(TomlConfig.class);

    public static final String PREFIX_VRV = "vrv";

    public static final String PREFIX_VRV_REGISTRY = "vrv.registry";
    public static final String PREFIX_VRV_REGISTRY_ZOOKEEPER = "vrv.registry.zookeeper";
    public static final String PREFIX_VRV_REGISTRY_LOCAL = "vrv.registry.local";

    public static final String PREFIX_VRV_RPC = "vrv.rpc";
    public static final String PREFIX_VRV_RPC_POOL = "vrv.rpc.pool";
    public static final String PREFIX_VRV_RPC_POOL_JAVA = "vrv.rpc.pool.java";

    public static final String PREFIX_VRV_SERVER = "vrv.server";

    public static final String PATTERN_MICRO_SERVER = "vrv.server.micro.{0}";
    public static final String PATTERN_MICRO_SCHEDULING = "vrv.server.micro.{0}.scheduling";
    public static final String PATTERN_MICRO_NET = "vrv.server.micro.{0}.net";

    public static final String PREFIX_VRV_QINQ = "vrv.QinQ";

    private TomlParseResult toml;

    private boolean distribute;
    private String log;
    private String model;
    private String inet;
    private String inetEnv;
    private String zookeeperAddress;
    private String zookeeperAddressEnv;
    private String zookeeperNamespace;
    private long rpcPoolMaxTotal;
    private long rpcPoolMaxIdle;
    private long rpcPoolMinIdle;
    private long rpcPoolConnTimeout;
    private long rpcPoolReadTimeout;
    private long rpcPoolWriteTimeout;
    private boolean rpcPoolJavaTestOnBorrow;
    private boolean rpcPoolJavaTestOnCreate;
    private boolean rpcPoolJavaTestOnReturn;
    private boolean rpcPoolJavaTestWhileIdle;
    private List<MicroConfig> microConfigList = new ArrayList<MicroConfig>();

    /**
     * 构造方法私有化
     *
     * @author chenlong
     * @date 2021-06-24 10:23:27
     */
    private TomlConfig() {
        readTomlFile();
    }

    // 实例
    private static TomlConfig tomlConfig = new TomlConfig();

    /**
     * 静态工厂方法
     *
     * @return
     * @author chenlong
     * @date 2021-06-24 10:23:20
     */
    public static TomlConfig getInstance() {
        return tomlConfig;
    }

    /**
     * 解析配置文件
     *
     * @author chenlong
     * @date 2021-06-24 10:23:58
     */
    private void readTomlFile() {
        try {
            // 读取server.toml配置文件
            InputStream in = null;
            String tomlPath = System.getProperty("toml");
            if (StringUtils.isNotEmpty(tomlPath)) {
                logger.debug("读取指定路径配置文件：{}", tomlPath);
                in = new FileInputStream(new File(tomlPath));
            } else {
                in = Thread.currentThread().getContextClassLoader().getResourceAsStream("server.toml");
            }

            if (null == in) {
                logger.error("服务配置文件toml读取失败.");
                return;
            }

            toml = Toml.parse(in);

            if (toml.hasErrors()) {
                toml.errors().forEach(error -> logger.error("Toml parse err", error));
                return;
            }

        } catch (Exception e) {
            logger.error("服务配置文件toml读取失败:", e);
            return;
        }

        // toml = new Toml().read(in);

        // URL url = TomlConfig.class.getClassLoader().getResource("server.toml");
        // System.out.println(url.getFile());
        // toml = new Toml().read(new File(url.getFile()));

        // 开始解析
        // [vrv]
        distribute = getBoolean(PREFIX_VRV + ".distribute");
        log = toml.getString(PREFIX_VRV + ".log");
        // [vrv.registry]
        model = toml.getString(PREFIX_VRV_REGISTRY + ".model");
        inet = toml.getString(PREFIX_VRV_REGISTRY + ".inet");
        inetEnv = toml.getString(PREFIX_VRV_REGISTRY + ".inetEnv");
        // [vrv.registry.zookeeper]
        zookeeperAddress = toml.getString(PREFIX_VRV_REGISTRY_ZOOKEEPER + ".address");
        zookeeperAddressEnv = toml.getString(PREFIX_VRV_REGISTRY_ZOOKEEPER + ".addressEnv");
        zookeeperNamespace = toml.getString(PREFIX_VRV_REGISTRY_ZOOKEEPER + ".namespace");
        // vrv.rpc.pool
        rpcPoolMaxTotal = getLong(PREFIX_VRV_RPC_POOL + ".maxTotal");
        rpcPoolMaxIdle = getLong(PREFIX_VRV_RPC_POOL + ".maxIdle");
        rpcPoolMinIdle = getLong(PREFIX_VRV_RPC_POOL + ".minIdle");
        rpcPoolConnTimeout = getLong(PREFIX_VRV_RPC_POOL + ".connTimeout");
        rpcPoolReadTimeout = getLong(PREFIX_VRV_RPC_POOL + ".readTimeout");
        rpcPoolWriteTimeout = getLong(PREFIX_VRV_RPC_POOL + ".writeTimeout");

        rpcPoolJavaTestOnBorrow = getBoolean(PREFIX_VRV_RPC_POOL_JAVA + ".testOnBorrow");
        rpcPoolJavaTestOnCreate = getBoolean(PREFIX_VRV_RPC_POOL_JAVA + ".testOnCreate");
        rpcPoolJavaTestOnReturn = getBoolean(PREFIX_VRV_RPC_POOL_JAVA + ".testOnReturn");
        rpcPoolJavaTestWhileIdle = getBoolean(PREFIX_VRV_RPC_POOL_JAVA + ".testWhileIdle");

        // Toml microTable = toml.getTable("vrv.server.micro");
        TomlTable microTable = toml.getTable("vrv.server.micro");

        if (microTable != null) {
            Map<String, Object> map = microTable.toMap();
            if (map != null && map.size() > 0) {
                for (String serverKey : map.keySet()) {
                    MicroConfig microConfig = new MicroConfig();
                    microConfig
                            .setName(toml.getString(MessageFormat.format(PATTERN_MICRO_SERVER, serverKey) + ".name"));
                    microConfig.setVersion(
                            toml.getString(MessageFormat.format(PATTERN_MICRO_SERVER, serverKey) + ".version"));
                    microConfig.setWeight(getLong(MessageFormat.format(PATTERN_MICRO_SERVER, serverKey) + ".weight"));

                    // cores/maxReadBuffer/threadsPerCore如果没有设置，是有默认值的，所以需要判断是否配置了，以避免没有设置的时候，被覆盖成0
                    if (toml.contains(MessageFormat.format(PATTERN_MICRO_SCHEDULING, serverKey) + ".cores")) {
                        microConfig.getSchedulingInfo().setCores(
                                getLong(MessageFormat.format(PATTERN_MICRO_SCHEDULING, serverKey) + ".cores"));
                    }
                    if (toml.contains(MessageFormat.format(PATTERN_MICRO_SCHEDULING, serverKey) + ".maxReadBuffer")) {
                        microConfig.getSchedulingInfo().setMaxReadBuffer(
                                getLong(MessageFormat.format(PATTERN_MICRO_SCHEDULING, serverKey) + ".maxReadBuffer"));
                    }
                    if (toml.contains(MessageFormat.format(PATTERN_MICRO_SCHEDULING, serverKey) + ".threadsPerCore")) {
                        microConfig.getSchedulingInfo().setThreadsPerCore(
                                getLong(MessageFormat.format(PATTERN_MICRO_SCHEDULING, serverKey) + ".threadsPerCore"));
                    }

                    microConfig.getNetInfo().setAddress(
                            toml.getString(MessageFormat.format(PATTERN_MICRO_NET, serverKey) + ".address"));
                    microConfig.getNetInfo().setNetwork(
                            toml.getString(MessageFormat.format(PATTERN_MICRO_NET, serverKey) + ".network"));
                    microConfig.getNetInfo().setProtocol(
                            toml.getString(MessageFormat.format(PATTERN_MICRO_NET, serverKey) + ".protocol"));
                    microConfigList.add(microConfig);
                }
            }
        }

    }

    /**
     * 获取用户自定义配置
     *
     * @return
     * @author chenlong
     * @date 2021-06-24 10:22:49
     */
    public String getQinQ() {
        TomlTable table = toml.getTable(PREFIX_VRV_QINQ);
        if (table != null) {
            return table.toJson();
        } else {
            return null;
        }
    }

    /**
     * 根据服务name获取该服务配置
     *
     * @param name
     * @return
     * @author chenlong
     * @date 2021-06-24 10:22:30
     */
    public MicroConfig getMicroConfig(String name) {
        for (MicroConfig mc : microConfigList) {
            if (mc.getName().equals(name)) {
                return mc;
            }
        }
        return new MicroConfig();
    }

    /**
     * 根据index获取单个服务配置
     *
     * @return
     */
    public MicroConfig getMicroConfig() {
        if (0 < microConfigList.size()) {
            return microConfigList.get(0);
        }
        return new MicroConfig();
    }

    /**
     * 校验micro参数是否正确
     *
     * @param microConfig
     * @return
     * @throws Exception
     */
    public static boolean checkMicroConfig(MicroConfig microConfig) throws Exception {

        if (StringUtils.isEmpty(microConfig.getName())) {
            throw new Exception("vrv.server.micro.-.name is empty.");
        }

        if (StringUtils.isEmpty(microConfig.getVersion())) {
            throw new Exception("vrv.server.micro.-.version is empty.");
        }

        if (microConfig.getSchedulingInfo().getCores() <= 0) {
            throw new Exception("vrv.server.micro.-.scheduling.cores must greater than zero.");
        }

        if (microConfig.getSchedulingInfo().getMaxReadBuffer() <= 0) {
            throw new Exception("vrv.server.micro.-.scheduling.maxReadBuffer must greater than zero.");
        }

        if (microConfig.getSchedulingInfo().getThreadsPerCore() <= 0) {
            throw new Exception("vrv.server.micro.-.scheduling.threadsPerCore must greater than zero.");
        }

        if (StringUtils.isEmpty(microConfig.getNetInfo().getAddress())) {
            throw new Exception("vrv.server.micro.-.net.address is empty.");
        }

        if (StringUtils.isEmpty(microConfig.getNetInfo().getNetwork())) {
            throw new Exception("vrv.server.micro.-.net.network is empty.");
        }

        // TODO 可以加上内部协议合法性验证
        if (StringUtils.isEmpty(microConfig.getNetInfo().getProtocol())) {
            throw new Exception("vrv.server.micro.-.net.protocol is empty.");
        }

        return true;
    }

    /**
     * 获取boolean类型值
     *
     * @param key
     * @return
     * @author chenlong
     * @date 2021-08-17 10:14:32
     */
    private boolean getBoolean(String key) {
        boolean value = false;
        Boolean temp = toml.getBoolean(key);
        if (temp != null) {
            value = temp;
        }
        return value;
    }

    /**
     * 获取long类型值
     *
     * @param key
     * @return
     * @author chenlong
     * @date 2021-08-17 10:17:53
     */
    private long getLong(String key) {
        long value = 0;
        Long temp = toml.getLong(key);
        if (temp != null) {
            value = temp;
        }
        return value;
    }

}
