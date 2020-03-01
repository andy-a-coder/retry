package com.andy.retry.core.model;

import lombok.Data;

/**
 * 延迟信息
 * @author andy
 *
 */
@Data
public class DelayDetail {
    /**
     * 延迟级别
     */
    private Integer delayLevel;
    /**
     * 该level到期后的剩余时间（设置的延迟时间于18个delay级别不匹配的情况，需要计算restSeconds）
     */
    private Long restSeconds;
}
