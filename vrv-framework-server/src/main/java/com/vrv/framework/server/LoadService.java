package com.vrv.framework.server;

/**
 * 加载服务实现类
 *
 * @author chenlong
 * @date 2021/9/1 10:04
 */
@FunctionalInterface
public interface LoadService {
    /**
     * 为什么加这个接口，主要是考虑服务实现可能有状态，比如缓存
     * 那么重启的时候，实际上是要清除这些状态的，所以类要完全重新加载。
     * 所以每次加载的时候，由实现者自己判断是否加载全新的实现类
     * 还是重用原来的实现类
     *
     * @return
     */
    Object loadServiceImpl();
}
