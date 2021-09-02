package com.vrv.example.server;


import com.vrv.example.server.impl.UserImpl;
import com.vrv.framework.logger.VrvLogger;
import com.vrv.framework.server.vrv.VrvServer;
import com.vrv.framework.server.VrvServerFactory;

/**
 * @author chenlong
 * @date 2021/9/1 16:53
 */
public class Starter {

    public static void main(String[] args) throws Exception {


//        TomlConfig tomlConfig = TomlConfig.getInstance();
//        System.out.println(tomlConfig.getMicroConfig());

        VrvLogger.initialize();

        VrvServer vrvServer = VrvServerFactory.DEFAULT.getVrvServer();
        vrvServer.setServiceImpl(() -> new UserImpl());
        vrvServer.start();

    }

}
