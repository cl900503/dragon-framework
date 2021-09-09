/**
 * @ GenericConnectionProvider.java Create on 2011-9-15 上午11:12:31
 */
package com.vrv.framework.client.provider.impl;

import com.vrv.framework.client.VoaTransport;
import com.vrv.framework.client.model.Node;
import com.vrv.framework.client.provider.ConnectionProvider;
import com.vrv.framework.client.transport.ThriftPoolableObjectFactory;
import com.vrv.framework.common.config.TomlConfig;
import com.vrv.framework.common.exception.VoaClientPoolException;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


/**
 * 对连接的管理类,有以下职责:<br>
 * 1. 连接池职责: 建立和唯一标识符(ip+port)对应的连接池信息<br>
 * 2. 继承自接口职责: 获取,归还连接<br>
 * 所有方法应该只抛出XoaClientPoolException
 *
 * @author chenlong
 */
public class TTransportConnectionProvider implements ConnectionProvider {

    static Logger logger = LoggerFactory.getLogger(TTransportConnectionProvider.class);
//
//    /** 连接超时配置 */
//    private static int connTimeout = 250;

    // 配置数据 从registry获取
    /** 可以从缓存池中分配对象的最大数量 */
//	private static int maxActive = 300;

    /** 缓存池中最大空闲对象数量 */
//	private static int maxIdle = 300;

    /** 缓存池中最小空闲对象数量 */
//    private static int minIdle = 50;

    /** 最多等待多少毫秒 */
//	private static long maxWait = 8;

    /**
     * 从缓存池中分配对象，是否执行PoolableObjectFactory.validateObject方法
     */
//	private boolean testOnBorrow = false;

//	private boolean testOnReturn = false;

//	private boolean testWhileIdle = false;

    private static HashMap<String, ObjectPool> servicePoolMap = new HashMap<String, ObjectPool>();

    public TTransportConnectionProvider() {
    }

//	public static void setPoolParam(int maxActive, int maxIdle, int minIdle, int maxWait) {
//		TTransportConnectionProvider.maxActive = maxActive;
//		TTransportConnectionProvider.maxIdle = maxIdle;
////        TTransportConnectionProvider.minIdle = minIdle;
//		TTransportConnectionProvider.maxWait = maxWait;
//	}

//    /**
//     * 设置连接的超时时间，如果需要针对独立的 IP+Port 设定超时时间，则需要调整 servicePoolMap 的数据结构
//     * 
//     * @param connTimeout
//     */
//    public static void setTimeout(int connTimeout) {
//        TTransportConnectionProvider.connTimeout = connTimeout;
//    }
//
//    /**
//     * XOASEC-147
//     *
//     *
//     * @return timeout
//     */
//    public static int getTimeout() {
//        return TTransportConnectionProvider.connTimeout;
//    }

//	ObjectPool createPool(Node node, int connTimeout) {
//		// 设置factory
//		ThriftPoolableObjectFactory thriftPoolableObjectFactory = new ThriftPoolableObjectFactory(node.getHost(), node.getPort(),
//				connTimeout);
//		GenericObjectPool objectPool = new GenericObjectPool(thriftPoolableObjectFactory);
//
//		TomlConfig tomlConfig = TomlConfig.getInstance();
//		
//		objectPool.setMaxActive(maxActive);
//		objectPool.setMaxIdle(maxIdle);
////        objectPool.setMinIdle(minIdle);
//		objectPool.setMaxWait(maxWait);
//		objectPool.setTestOnBorrow(testOnBorrow);
//		objectPool.setTestOnReturn(testOnReturn);
//		objectPool.setTestWhileIdle(testWhileIdle);
//		// 连接池耗尽，borrowObject方法锁等待
//		objectPool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_FAIL);
//
//		return objectPool;
//	}

    ObjectPool createPool(Node node, int connTimeout) {

        TomlConfig tomlConfig = TomlConfig.getInstance();

        // 设置factory
        ThriftPoolableObjectFactory thriftPoolableObjectFactory = new ThriftPoolableObjectFactory(node.getHost(), node.getPort(),
                connTimeout);

        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(Integer.valueOf(String.valueOf(tomlConfig.getRpcPoolMaxTotal())));
        config.setMaxIdle(Integer.valueOf(String.valueOf(tomlConfig.getRpcPoolMaxIdle())));
        config.setMinIdle(Integer.valueOf(String.valueOf(tomlConfig.getRpcPoolMinIdle())));

        GenericObjectPool objectPool = new GenericObjectPool(thriftPoolableObjectFactory, config);
        objectPool.setMaxWaitMillis(tomlConfig.getRpcPoolConnTimeout());

        objectPool.setTestOnBorrow(tomlConfig.isRpcPoolJavaTestOnBorrow());
        objectPool.setTestOnCreate(tomlConfig.isRpcPoolJavaTestOnCreate());
        objectPool.setTestOnReturn(tomlConfig.isRpcPoolJavaTestOnReturn());
        objectPool.setTestWhileIdle(tomlConfig.isRpcPoolJavaTestWhileIdle());

        return objectPool;
    }

    public static String getConnStatus() {
        StringBuffer message = new StringBuffer();
        for (Map.Entry<?, ?> entry : servicePoolMap.entrySet()) {
            String keyString = (String) entry.getKey();
            ObjectPool pool = (ObjectPool) entry.getValue();

            message.append("Status of connection [" + keyString + "] is:" + "\n pool using size: " + pool.getNumActive()
                    + "\n pool idle size:" + pool.getNumIdle() + '\n');
        }

        return message.toString();
    }

    @Override
    public TTransport getConnection(Node node, long conTimeOut) throws Exception {
        TTransport transport = null;
        String key = node.getNodeKey();
        ObjectPool pool = null;

        try {
            pool = servicePoolMap.get(key);
            if (pool == null) {
                synchronized (key.intern()) {
                    if (!servicePoolMap.containsKey(key)) {
                        pool = createPool(node, (int) conTimeOut);
                        logger.info("pool-stat: pool construction " + key);
                        servicePoolMap.put(key, pool);
                    } else {
                        pool = servicePoolMap.get(key);
                    }
                }
            }
            transport = (TTransport) pool.borrowObject();

            if (logger.isDebugEnabled()) {
                logger.debug("pool-stat: alloc " + transport + ",active=" + pool.getNumActive() + ",idle=" + pool.getNumIdle());
            }
            return transport;
        } catch (java.util.NoSuchElementException e) {
            logger.warn("client pool exhausted and cannot or will not return another instance : " + key + ",active=" + pool.getNumActive()
                    + ",idle=" + pool.getNumIdle());
            throw new VoaClientPoolException(e);
        } catch (java.lang.IllegalStateException e) {
            logger.warn("client pool you called has been closed.");
            throw new VoaClientPoolException(e);
        } catch (Exception e) {
            logger.warn("client pool other exception : " + key + "ex=" + e.getMessage());
            throw new VoaClientPoolException(e);
        }
    }

    @Override
    public void returnConnection(VoaTransport xoaTransport) throws Exception {
        String key = xoaTransport.getNode().getNodeKey();
        ObjectPool pool = null;
        try {
            pool = servicePoolMap.get(key);
            if (pool != null) {
                if (xoaTransport.getTransport() != null) {
                    pool.returnObject(xoaTransport.getTransport());
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("pool-stat: dealloc " + xoaTransport.getTransport() + ",active=" + pool.getNumActive() + ",idle="
                            + pool.getNumIdle());
                }
            } else if (logger.isDebugEnabled()) {
                logger.debug("pool-stat: dealloc " + xoaTransport.getTransport() + ", pool not exist.");
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void invalidateConnection(VoaTransport xoaTransport) {
        String key = xoaTransport.getNode().getNodeKey();
        TTransport trans = xoaTransport.getTransport();
        ObjectPool pool = null;
        try {
            pool = servicePoolMap.get(key);
            if (pool != null) {
                if (trans != null) {
                    pool.invalidateObject(trans);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("pool-stat: invalidate " + trans + ",active=" + pool.getNumActive() + ",idle=" + pool.getNumIdle());
                }
            }
        } catch (Exception e) {
            logger.warn("failed to invalidateConnection.", e);
        }
    }

    @Override
    public void clearConnections(Node node) {
        ObjectPool pool = null;
        String key = node.getNodeKey();
        try {
            pool = servicePoolMap.get(key);
            if (pool != null) {
                pool.clear();
                // servicePoolMap.remove(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("pool-stat: pool destruction " + key);
    }

    @Override
    public void initConnectionPool(Node node, long conTimeOut) {
        String key = node.getNodeKey();
        ObjectPool pool = servicePoolMap.get(key);
        if (pool == null) {
            synchronized (key.intern()) {
                if (!servicePoolMap.containsKey(key)) {
                    pool = createPool(node, (int) conTimeOut);
                    servicePoolMap.put(key, pool);
                    logger.info("pool-stat: pool construction " + key);
                }
            }
        }
    }

//    public int getConnTimeout() {
//        return connTimeout;
//    }

//	public boolean isTestOnBorrow() {
//		return testOnBorrow;
//	}
//
//	public void setTestOnBorrow(boolean testOnBorrow) {
//		this.testOnBorrow = testOnBorrow;
//	}
//
//	public boolean isTestOnReturn() {
//		return testOnReturn;
//	}
//
//	public void setTestOnReturn(boolean testOnReturn) {
//		this.testOnReturn = testOnReturn;
//	}
//
//	public boolean isTestWhileIdle() {
//		return testWhileIdle;
//	}
//
//	public void setTestWhileIdle(boolean testWhileIdle) {
//		this.testWhileIdle = testWhileIdle;
//	}

}
