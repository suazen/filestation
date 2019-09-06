package me.daylight.filestation.authority;

import java.lang.annotation.*;

/**
 * @author Daylight
 * @date 2019/1/8 15:53
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Limit {
    boolean loginRequired() default true;
    ReturnType returnType() default ReturnType.Page;
}
