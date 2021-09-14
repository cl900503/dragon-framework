package com.vrv.framework.common.intercept;

import java.util.*;

/**
 * @author chenlong
 * @date 2021/9/1 10:46
 */
public class VrvServerInterceptHelp {



    /**
     * 服务端拦截器集合
     */
    private List<VrvServerInterceptHandle> interceptHandles = new ArrayList<>();

    public void addIntercept(VrvServerInterceptHandle vrvServerInterceptHandle) {
        interceptHandles.add(vrvServerInterceptHandle);
        // 根据order进行排序
        Collections.sort(interceptHandles, Comparator.comparingInt(VrvServerInterceptHandle::getOrder).reversed());
    }

    public List<VrvServerInterceptHandle> getInterceptHandles() {
        return interceptHandles;
    }

    /**
     * 构造方法
     *
     * @author chenlong
     * @date 2021-06-18 13:43:39
     */
    public VrvServerInterceptHelp() {
        // 通过java SPI机制加载所有服务端拦截器
        ServiceLoader<VrvServerInterceptHandle> service = ServiceLoader.load(VrvServerInterceptHandle.class);
        for (VrvServerInterceptHandle i : service) {
            addIntercept(i);
        }
    }


}
