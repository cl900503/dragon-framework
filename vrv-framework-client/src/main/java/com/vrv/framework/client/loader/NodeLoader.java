package com.vrv.framework.client.loader;

import com.vrv.framework.client.filter.NodeFilter;
import com.vrv.framework.client.model.Node;

import java.util.List;

/**
 * 用于load node节点信息
 *
 * @author chenlong
 * @date 2021/9/9 15:56
 */
public interface NodeLoader {


    /**
     * 根据serviceId 和 version load 所有节点信息（默认shard为0）
     *
     * @param serviceId
     * @param version
     * @param filter    节点filter
     * @return
     */
    public List<Node> load(String serviceId, String version, NodeFilter filter);

    /**
     * 根据serviceId+version+shard load所有节点信息
     *
     * @param serviceId
     * @param version
     * @param shardParam
     * @param filter     节点filter
     * @return
     */
    public List<Node> load(String serviceId, String version, int shardParam, NodeFilter filter);

    /**
     * load serviceId + version 所有shard的节点信息
     *
     * @param serviceId
     * @param version
     * @return
     */
    public List<Node> loadAll(String serviceId, String version);

    /**
     * load serviceId + version 所有shard的节点信息
     *
     * @param serviceId
     * @param version
     * @param filter    节点filter
     * @return
     */
    public List<Node> loadAll(String serviceId, String version, NodeFilter filter);

}
