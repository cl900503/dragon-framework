package com.vrv.framework.logger;


import com.vrv.framework.common.config.TomlConfig;

/**
 * @author chenlong
 * @date 2021/8/31 17:14
 */
public class Test {

    public static void main(String[] args) {

        TomlConfig tomlConfig = TomlConfig.getInstance();
        System.out.println(tomlConfig.getMicroConfig());

    }

}
