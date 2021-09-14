package com.vrv.example.server.impl;

import com.vrv.example.base.thrift.User;
import com.vrv.example.base.thrift.UserService;
import com.vrv.framework.server.service.impl.VrvServiceBase;
import org.apache.thrift.TException;

/**
 * @author chenlong
 * @date 2021/9/1 16:40
 */
public class UserImpl extends VrvServiceBase implements UserService.Iface {
    /**
     * 获取用户信息
     *
     * @param userId
     */
    @Override
    public User getUser(long userId) throws TException {
        User user = new User();
        user.setUserId(1001);
        user.setUserName("chenlong");
        return user;
    }
}
