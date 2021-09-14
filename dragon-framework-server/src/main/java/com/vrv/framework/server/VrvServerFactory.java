package com.vrv.framework.server;

import com.vrv.framework.server.vrv.VrvServer;
import com.vrv.framework.server.vrv.impl.thrift.VrvTThreadedSelectorServer;

/**
 * VrvServer
 *
 * @author chenlong
 * @date 2021/8/31 17:56
 */
public enum VrvServerFactory {
    /**
     * 默认 TThreadedSelectorServer
     */
    DEFAULT() {
        @Override
        public VrvServer getVrvServer() {

            return new VrvTThreadedSelectorServer();
        }
    };

    /**
     * getServer
     *
     * @return
     */
    public abstract VrvServer getVrvServer();

}
