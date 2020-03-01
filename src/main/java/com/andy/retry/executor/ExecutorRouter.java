package com.andy.retry.executor;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 重试执行程序路由
 * @author andy
 *
 */
@SuppressWarnings("rawtypes")
@Component
public class ExecutorRouter {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    /**
     * 存放重试执行程序实例的容器
     * key: 重试业务名称
     * value: 重试业务执行程序的bean
     */
    
    private Map<String,RetryExecutor> executors = null;
    
    @PostConstruct
    public void initExecutors(){
        executors = applicationContext.getBeansOfType(RetryExecutor.class);
    }
    
    public RetryExecutor route(String retryName) {
        if(executors!=null)
            return executors.get(retryName);
        return null;
    }
    
}
