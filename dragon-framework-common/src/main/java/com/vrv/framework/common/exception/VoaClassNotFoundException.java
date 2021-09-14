package com.vrv.framework.common.exception;

/**
 * runtime异常，用于代替{@link ClassNotFoundException}
 *
 * @author chenlong
 * @date 2021/9/9 15:03
 */
public class VoaClassNotFoundException extends VoaRuntimeException {
    private static final long serialVersionUID = -4926184400076330275L;

    public VoaClassNotFoundException(Throwable cause) {
        super(cause);
    }
}
