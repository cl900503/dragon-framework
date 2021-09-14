package com.vrv.framework.common.utils.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;

/**
 * JacksonUtil
 * @author chenlong
 * @date 2021/8/31 16:32
 */
public class JacksonUtil {


    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 忽略未知属性
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @SneakyThrows
    public static String toJSONString(Object object) {
        return objectMapper.writeValueAsString(object);
    }

    @SneakyThrows
    public static <T> T parseObject(String text, Class<T> clazz) {
        return objectMapper.readValue(text, clazz);
    }

    @SneakyThrows
    public static <T> List<T> parseArray(String text, Class<T> clazz) {
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(ArrayList.class, clazz);
        return objectMapper.readValue(text, javaType);
    }

    public ObjectMapper newObjectMapper() {
        return new ObjectMapper();
    }


}
