package com.vrv.example.client;

import com.vrv.example.base.thrift.User;
import com.vrv.example.base.thrift.UserService;
import com.vrv.framework.client.ServiceFactory;
import org.apache.thrift.TException;

/**
 * @author chenlong
 * @date 2021/8/27 18:22
 */
public class Client {

    public static void main(String[] args) throws TException {

//        TFramedTransport transport = new TFramedTransport(new TSocket("127.0.0.1", 11242));
//        transport.open();
//        // 设置传输协议为 TBinaryProtocol
//        TProtocol protocol = new TBinaryProtocol(transport);
//        UserService.Client client = new UserService.Client(protocol);
//
//        User user = client.getUser(0L);

        IUserService client = ServiceFactory.getService(IUserService.class, 5000);
        User user = client.getUser(0L);
        System.out.println(user);

    }

}
