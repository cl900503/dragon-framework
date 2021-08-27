package com.vrv.server;

import com.vrv.framework.logger.VrvLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author chenlong
 */
public class Test{

    private static final Logger logger = LoggerFactory.getLogger(Test.class);

    /**
     * 测试
     * @param args
     */
    public static void main(String[] args) {

        VrvLogger.initialize(null);

        while(true) {
            logger.debug("测试");
        }
    }


}