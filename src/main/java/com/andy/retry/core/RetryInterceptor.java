package com.andy.retry.core;

import javax.annotation.Resource;

import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.andy.retry.annotation.Retry;
import com.andy.retry.core.model.DelayDetail;
import com.andy.retry.core.model.RetryMsg;
import com.andy.retry.executor.ExecutorRouter;
import com.andy.retry.executor.RetryExecutor;

import lombok.extern.slf4j.Slf4j;

/**
 * 重试注解的拦截器
 * @author andy
 *
 */
@Aspect
@Component
@Order(-1)
@Slf4j
public class RetryInterceptor {

    @Resource
    private RocketMQTemplate rocketMQTemplate;
    // 消息重试的topic
    @Value("r-topic-${spring.application.name}")
    private String topic;
    // 消息重试的tag
    public static final String RETRY_TAG = "r-tag-andy";
    
    @Resource
    private ExecutorRouter executorRouter;
    
    /**
     * 执行业务方法，如果失败，将信息根据配置的重试策略放入mq的延迟队列
     * @param joinPoint
     * @param retry
     * @return
     * @throws Throwable
     */
    @Around("@annotation(retry)")
    public Object proceed(ProceedingJoinPoint joinPoint,Retry retry) throws Throwable {
        try {
            if(preCheck())
                return joinPoint.proceed();
        } catch (Throwable e) {
            log.error("####### call notice biz error",e);
            sendToMqOnError(retry, joinPoint);
        }
        return null;
    }

    /**
     * 执行检查，如果通知时间未到，根据策略配置修改参数后直接放回mq（没有精准匹配延迟队列的情况）
     * @return
     */
    private Boolean preCheck() {
        RetryMsg retryMsg = RetryManager.retryMsg.get();
        if(retryMsg != null && retryMsg.getRestSeconds() > 0) {
            DelayDetail delayLevel = DelayLevelTool.getDelayLevel(retryMsg.getRestSeconds());
            retryMsg.setRestSeconds(delayLevel.getRestSeconds());
            GenericMessage<RetryMsg> msg = new GenericMessage<>(retryMsg);
            SendResult sendResult = rocketMQTemplate.syncSend(String.format("%s:%s", topic, RETRY_TAG), msg, rocketMQTemplate.getProducer().getSendMsgTimeout(), delayLevel.getDelayLevel());
            log.info("####### call biz time not arrived，put message into mq delay queue, delayLevel: {}, retryMsg: {}, sendResult: {}", delayLevel.getDelayLevel(), retryMsg, sendResult);
            return false;
        }
        return true;
    }

    /**
     * 通知失败后，根据重试策略配置将消息发送到mq
     * @param retry
     * @param methodArgs
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void sendToMqOnError(Retry retry, ProceedingJoinPoint joinPoint) {
        String[] retryStrategy = retry.retryStrategy();
        RetryMsg retryMsg = RetryManager.retryMsg.get();
        if(retryMsg != null) {
            if(retryMsg.getOrder() >= retryStrategy.length) {
                log.info("####### number of retry times run out, call onFinallFailCallBack now， message: {}", retryMsg);
                RetryExecutor executor = executorRouter.route(retryMsg.getRetryName());
                if(executor!=null) {
                    executor.onFinallFailCallBack(JSON.parseObject(retryMsg.getMsg(), retryMsg.getMsgClass()));
                }
                return;
            }
        } else {
            Object[] methodArgs = joinPoint.getArgs();
            retryMsg = new RetryMsg(getBeanName(joinPoint), 0, JSON.toJSONString(methodArgs[0]), methodArgs[0].getClass());
        }
        DelayDetail delayLevel = DelayLevelTool.getDelayLevel(DelayLevelTool.getTimeSeconds(retryStrategy[retryMsg.getOrder()]));
        retryMsg.setRestSeconds(delayLevel.getRestSeconds());
        retryMsg.setOrder(retryMsg.getOrder()+1);
        GenericMessage<RetryMsg> msg = new GenericMessage<>(retryMsg);
        SendResult sendResult = rocketMQTemplate.syncSend(String.format("%s:%s", topic, RETRY_TAG), msg, rocketMQTemplate.getProducer().getSendMsgTimeout(), delayLevel.getDelayLevel());
        log.info("####### put message into mq delay queue, delayLevel: {}, retryMsg: {}, sendResult: {}", delayLevel.getDelayLevel(), retryMsg, sendResult);
    }

    /**
     * 获取业务bean名称
     * @param methodSignature
     * @return
     */
    private String getBeanName(ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String className = methodSignature.getDeclaringTypeName();
        if(className.indexOf(".")>-1)
            className = className.substring(className.lastIndexOf(".")+1);
        return className.substring(0,1).toLowerCase() + className.substring(1);
    }
}
