package com.vrv.framework.client.annotation;

import java.lang.annotation.*;


/**
 * @author chenlong
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface VoaService {

    /**
     * @return service ID
     */
    String value();

    String version() default "1+";

    int timeout() default 5000;
}
