package com.vrv.example.client;

import com.vrv.example.base.thriftbean.User;
import com.vrv.example.base.thriftbean.UserService;
import com.vrv.framework.common.utils.CommonUtil;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.layered.TFramedTransport;

/**
 * @author chenlong
 * @date 2021/8/27 18:22
 */
public class Client {

    public static void main(String[] args) throws TException {

        TFramedTransport transport = new TFramedTransport(new TSocket("172.24.192.1", 11242));
        transport.open();
        // 设置传输协议为 TBinaryProtocol
        TProtocol protocol = new TBinaryProtocol(transport);
        UserService.Client client = new UserService.Client(protocol);

        User user = client.getUser(0L);

        System.out.println(user);

    }

}
