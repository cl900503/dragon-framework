package com.vrv.example.logger;

import com.vrv.framework.logger.VrvLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chenlong
 * @date 2021/8/27 16:35
 */
public class VrvLoggerTest {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(VrvLoggerTest.class);

    public static void main(String[] args) {

        // 初始化日志框架
        VrvLogger.initialize();

        for (int i = 1; i <= 10; i++) {
            logger.debug("测试：" + i);
        }
    }

}
