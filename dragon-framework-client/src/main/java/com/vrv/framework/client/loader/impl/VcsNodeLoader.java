package com.vrv.framework.client.loader.impl;


import com.vrv.framework.client.filter.NodeFilter;
import com.vrv.framework.client.loader.NodeLoader;
import com.vrv.framework.client.model.Node;
import com.vrv.framework.client.model.Service;
import com.vrv.framework.client.model.Shard;
import com.vrv.framework.client.registry.VoaRegistry;
import com.vrv.framework.client.registry.VoaRegistryFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 利用xcs获取节点信息的loader实现
 *
 * @author chenlong
 * @date 2021/9/9 16:05
 */
public class VcsNodeLoader implements NodeLoader {

    private static VoaRegistryFactory xoaRegistryFactory = VoaRegistryFactory.getInstance();

    /**
     * For Unit-test, we need to inject mock fields.
     *
     * @param xoaRegistryFac
     */
    protected static void setXoaRegistryFactory(VoaRegistryFactory xoaRegistryFac) {
        xoaRegistryFactory = xoaRegistryFac;
    }

    private VoaRegistry xoaRegistry = xoaRegistryFactory.getDefaultRegistry();

    public void setXoaRegistry(VoaRegistry xoaRegistry) {
        this.xoaRegistry = xoaRegistry;
    }

    @Override
    public List<Node> load(String serviceId, String version, NodeFilter filter) {
        return load(serviceId, version, 0, filter);
    }

    @Override
    public List<Node> load(String serviceId, String version, int shardParam, NodeFilter filter) {
        Service service = xoaRegistry.queryService(serviceId, version);
        Shard shard = service.getShards().get(shardParam);
        List<Node> nodes = Collections.emptyList();
        if (shard != null) {
            nodes = shard.getNodes();
        }
        if (filter != null)
            nodes = filter.filterNode(nodes);
        if (nodes == null)
            nodes = Collections.emptyList();
        return nodes;
    }

    @Override
    public List<Node> loadAll(String serviceId, String version) {
        Service service = xoaRegistry.queryService(serviceId, version);
        Map<Integer, Shard> shards = service.getShards();
        List<Node> nodes = new ArrayList<Node>();
        for (Map.Entry<Integer, Shard> entry : shards.entrySet()) {
            nodes.addAll(entry.getValue().getNodes());
        }
        return nodes;
    }

    @Override
    public List<Node> loadAll(String serviceId, String version, NodeFilter filter) {
        Service service = xoaRegistry.queryService(serviceId, version);
        Map<Integer, Shard> shards = service.getShards();
        List<Node> nodes = new ArrayList<Node>();
        for (Map.Entry<Integer, Shard> entry : shards.entrySet()) {
            nodes.addAll(entry.getValue().getNodes());
        }
        if (filter != null) {
            nodes = filter.filterNode(nodes);
        }
        return nodes;
    }

}