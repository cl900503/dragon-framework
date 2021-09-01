package com.vrv.example.server;

import com.vrv.example.server.impl.UserImpl;
import com.vrv.framework.logger.VrvLogger;
import com.vrv.framework.server.ServerFactory;
import com.vrv.framework.server.VrvServer;

/**
 * @author chenlong
 * @date 2021/9/1 16:53
 */
public class Starter {

    public static void main(String[] args) throws Exception {


//        TomlConfig tomlConfig = TomlConfig.getInstance();
//        System.out.println(tomlConfig.getMicroConfig());

        VrvLogger.initialize();

        VrvServer vrvServer = ServerFactory.DEFAULT.getServer();
        vrvServer.setServiceImpl(() -> new UserImpl());
        vrvServer.start();

    }

}
