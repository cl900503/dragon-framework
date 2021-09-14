package com.vrv.framework.client.registry;


/**
 * XoaRegistry工厂，支持自动注册XCS
 * XOA2新版，收集本地服务并基于zk完成注册
 *
 * @author chenlong
 */
public class VoaRegistryFactory {

    private static VoaRegistryFactory instance = new VoaRegistryFactory();

    public static VoaRegistryFactory getInstance() {
        return instance;
    }

    private VoaRegistry registry;

    private VoaRegistryFactory() {
        //registry = new IMRegistryImpl(new FindServiceRegistryFactory());
        registry = new VoaRegistryImplNew();
    }

    /**
     * @return 默认的XoaRegistry
     */
    public VoaRegistry getDefaultRegistry() {
        return registry;
    }
}
