package com.vrv.framework.common.exception;

/**
 * 无可用service node时抛出的异常
 *
 * @author chenlong
 * @date 2021/9/9 15:11
 */
public class VoaNoneAvailableNodeException extends VoaRuntimeException {

    private static final long serialVersionUID = -8205352455222005848L;

    public VoaNoneAvailableNodeException(String msg) {
        super(msg);
    }

    public VoaNoneAvailableNodeException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public VoaNoneAvailableNodeException(Throwable cause) {
        super(cause);
    }
}
