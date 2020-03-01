package com.andy.retry.core;

import javax.annotation.Resource;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.andy.retry.core.model.RetryMsg;
import com.andy.retry.executor.ExecutorRouter;
import com.andy.retry.executor.RetryExecutor;

import lombok.extern.slf4j.Slf4j;

/**
 * 消息重试管理（从mq中接收需重试的消息，根据配置调用对应的业务执行程序进行重试）
 * @author andy
 *
 */
@SuppressWarnings("rawtypes")
@Component
@RocketMQMessageListener(topic = "r-topic-${spring.application.name}", selectorExpression = "r-tag-andy", consumerGroup = "r-cg-${spring.application.name}")
@Slf4j
public class RetryManager implements RocketMQListener<RetryMsg> {
    
    public static final ThreadLocal<RetryMsg> retryMsg = new ThreadLocal<>();
    
    @Resource
    private ExecutorRouter executorRouter;
    
    /**
     * 从mq中接收需重试的消息，根据配置调用对应的业务执行程序进行重试，如果失败，按策略放入延迟队列
     */
    @SuppressWarnings("unchecked")
    @Override
    public void onMessage(RetryMsg message) {
        log.info("####### received retry message : {}", message);
        RetryExecutor executor = executorRouter.route(message.getRetryName());
        if(executor!=null) {
            RetryManager.retryMsg.set(message);
            executor.execute(JSON.parseObject(message.getMsg(), message.getMsgClass()));
        } else {
            log.info("####### no handler found，message: {}", message);
            throw new RuntimeException(String.format("no handler found，message: %s", JSON.toJSONString(message))) ;
        }
    }
}
