/**
 * @ ConnectionProvider.java Create on 2011-9-15 上午11:19:58
 */
package com.vrv.framework.client.provider;

import com.vrv.framework.client.VoaTransport;
import com.vrv.framework.client.model.Node;
import org.apache.thrift.transport.TTransport;


/**
 * TODO: 接口处于不一致情况, 尽快修复<br>
 * 		获取的是 TTransport, 返回的却是 XoaTransport
 *
 * @author chenlong
 */
public interface ConnectionProvider {
    /**
     * 获取一个链接
     *
     * @param node 连接描述信息 {@link Node}
     * @return
     */
    public TTransport getConnection(Node node, long conTimeOut) throws Exception;

    /**
     * 返回链接<br>
     * <p>
     * 如果链接不是由 getConnection 返回的，则会抛出异常
     *
     * @param xoaTransport
     */
    public void returnConnection(VoaTransport xoaTransport) throws Exception;


    public void invalidateConnection(VoaTransport xoaTransport);

    public void clearConnections(Node node);

    void initConnectionPool(Node node, long conTimeOut);

}
  