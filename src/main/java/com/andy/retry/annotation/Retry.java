package com.andy.retry.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 供通知重试配置使用的注解
 * @author andy
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Retry {
    /**
     * 重试策略，如：5s,10s,1m,1h,1d
     */
    String[] retryStrategy();
    
}