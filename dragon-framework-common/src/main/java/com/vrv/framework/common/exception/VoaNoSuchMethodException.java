package com.vrv.framework.common.exception;

/**
 * runtime异常，用于代替{@link NoSuchMethodException}
 *
 * @author chenlong
 * @date 2021/9/9 15:08
 */
public class VoaNoSuchMethodException extends VoaRuntimeException {

    private static final long serialVersionUID = -7450847717030521438L;

    public VoaNoSuchMethodException(Throwable cause) {
        super(cause);
    }

}
