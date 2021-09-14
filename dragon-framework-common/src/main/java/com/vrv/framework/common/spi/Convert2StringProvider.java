package com.vrv.framework.common.spi;

import java.util.Iterator;
import java.util.ServiceLoader;

import com.vrv.framework.common.spi.string.Convert2String;
import com.vrv.framework.common.spi.string.impl.DefaultConvert2String;
import lombok.extern.slf4j.Slf4j;

/**
 * convert to string provider 日志输出用
 *
 * @author jingshouyan 2021-04-28 17:22
 **/
@Slf4j
public class Convert2StringProvider {

    private static final Convert2String CONVERT2_STRING;

    static {
        ServiceLoader<Convert2String> convert2Strings = ServiceLoader.load(Convert2String.class);
        Iterator<Convert2String> iterator = convert2Strings.iterator();
        if (iterator.hasNext()) {
            CONVERT2_STRING = iterator.next();
        } else {
            CONVERT2_STRING = new DefaultConvert2String();
        }
        log.debug("use {}", CONVERT2_STRING.name());
    }

    public static Convert2String getConvert2String() {
        return CONVERT2_STRING;
    }
}
