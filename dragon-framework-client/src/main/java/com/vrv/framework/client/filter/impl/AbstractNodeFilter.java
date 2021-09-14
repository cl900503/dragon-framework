package com.vrv.framework.client.filter.impl;

import com.vrv.framework.client.filter.NodeFilter;
import com.vrv.framework.client.model.Node;

import java.util.List;

/**
 * 节点过滤器NodeFilter的抽象实现类
 *
 * @author chenlong
 * @date 2021/9/9 16:16
 */
public abstract class AbstractNodeFilter implements NodeFilter {
    protected final String serverAreaId;

    public AbstractNodeFilter(String serverAreaId) {
        this.serverAreaId = serverAreaId;
    }

    public String getServerAreaId() {
        return serverAreaId;
    }

    @Override
    public abstract List<Node> filterNode(List<Node> nodes);

}
