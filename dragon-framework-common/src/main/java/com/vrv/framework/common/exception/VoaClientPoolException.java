package com.vrv.framework.common.exception;

/**
 * @author chenlong
 */
public class VoaClientPoolException extends VoaRuntimeException {


    private static final long serialVersionUID = -3284286505461918550L;

    public VoaClientPoolException(String msg) {
        super(msg);
    }

    public VoaClientPoolException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public VoaClientPoolException(Throwable cause) {
        super(cause);
    }


}
