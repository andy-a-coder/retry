package com.andy.retry.core.model;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 重试消息模型
 * @author andy
 *
 */
@Data
public class RetryMsg implements Serializable {
    private static final long serialVersionUID = 1L;

    public RetryMsg() {}
    
    public RetryMsg(String retryName, int order, String msg, Class<?> msgClass) {
        this.retryName = retryName;
        this.order = order;
        this.msg = msg;
        this.msgClass = msgClass;
    }
    /**
     * 重试配置名称
     */
    private String retryName;
    /**
     * 消息内容
     */
    private String msg;
    /**
     * 消息内容Class
     */
    private Class<?> msgClass;
    /**
     * 第几次
     */
    private int order;
    /**
     * 拿到消息后，距离本次通知还有多少时间（如果剩余时间大于0，则继续放入mq的延迟队列）
     */
    private long restSeconds;

}
