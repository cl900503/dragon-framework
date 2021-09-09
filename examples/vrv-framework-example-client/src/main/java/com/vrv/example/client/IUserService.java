package com.vrv.example.client;

import com.vrv.example.base.thrift.UserService;
import com.vrv.framework.client.annotation.VoaService;

/**
 * @author chenlong
 * @date 2021/9/9 17:57
 */
@VoaService(value="user",version="1.0")
public interface IUserService  extends UserService.Iface{
}
