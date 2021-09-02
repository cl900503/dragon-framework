package com.vrv.framework.common.spi.string.impl;


import com.vrv.framework.common.spi.string.Convert2String;
import com.vrv.framework.common.utils.json.JacksonUtil;

/**
 * DefaultConvert2String
 *
 * @author chenlong
 * @date 2021/8/31 16:15
 */
public class DefaultConvert2String implements Convert2String {

    @Override
    public String name() {
        return "DefaultConvert2String";
    }

    @Override
    public String convert2String(Object obj) {
        return JacksonUtil.toJSONString(obj);
    }
}
