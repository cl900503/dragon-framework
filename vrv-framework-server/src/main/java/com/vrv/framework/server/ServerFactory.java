package com.vrv.framework.server;

/**
 * ServerFactory
 *
 * @author chenlong
 * @date 2021/8/31 17:56
 */
public enum ServerFactory {
    /**
     * 默认 TThreadedSelectorServer
     */
    DEFAULT() {
        @Override
        public VrvServer getServer() {

            return new VrvTThreadedSelectorServer();
        }
    };

    /**
     * getServer
     *
     * @return
     */
    public abstract VrvServer getServer();

}
