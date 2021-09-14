package com.vrv.framework.common.exception;

import org.apache.thrift.TException;

import java.lang.reflect.UndeclaredThrowableException;

/**
 * xoa的根异常。
 * 由于业务接口继承至thrift生成的Iface时，把抛出{@link TException}的声明去掉，框架在
 * 抛出Checked Exception时，会同时抛出{@link UndeclaredThrowableException}异常。
 * 所以XOA框架不对外抛出任何CheckedException，只抛出{@link VoaRuntimeException}。
 *
 * @author chenlong
 * @date 2021/9/9 15:24
 */
public class VoaRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 238889144225830369L;

    public VoaRuntimeException(String msg) {
        super(msg);
    }

    public VoaRuntimeException(Throwable cause) {
        super(cause);
    }

    public VoaRuntimeException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
