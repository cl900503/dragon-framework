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

//        Map<String, String> params = new HashMap<>();
//        // 日志输出路径
//        params.put("logPath","/data/log");
//        // 初始化日志框架
//        VrvLogger.initialize(params);

        // 初始化日志框架
        VrvLogger.initialize();

        for (int i = 1; i <= 10; i++) {
            logger.debug("测试：" + i);
            logger.info("测试：" + i);
        }

        // [:2021-08-27 16:51:10,492:] [:DEBUG:] [:main:] [:demoServer-22468:] [:com.vrv.example.logger.VrvLoggerTest::测试：10:]
    }

}
