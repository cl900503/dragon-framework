package com.vrv.framework.common.exception;

/**
 * 获得transport为null或者不可用抛出这个异常
 *
 * @author chenlong
 */
public class VoaTransportException extends VoaRuntimeException {


    private static final long serialVersionUID = 7833778650085409706L;

    public VoaTransportException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public VoaTransportException(String msg) {
        super(msg);
    }

    public VoaTransportException(Throwable cause) {
        super(cause);
    }

}
