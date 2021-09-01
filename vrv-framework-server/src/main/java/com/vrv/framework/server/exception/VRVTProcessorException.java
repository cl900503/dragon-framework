package com.vrv.framework.server.exception;

/**
 * @author chenlong
 * @date 2021/9/1 10:36
 */
public class VRVTProcessorException  extends Exception{
    private static final long serialVersionUID = 1L;
    public VRVTProcessorException(String error){
        super(error);
    }
}
