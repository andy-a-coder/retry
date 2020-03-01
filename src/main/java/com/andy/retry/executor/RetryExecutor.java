package com.andy.retry.executor;

/**
 * 通知和重试执行程序接口(每个重试业务需有各自的实现)
 * @author andy
 *
 */
public interface RetryExecutor<P> {
    /**
     * 如果执行失败，需重试的业务方法
     * @param bizModel
     */
    public <R> R execute(P bizModel);
    
    /**
     * 最终重试失败的处理策略
     * @param bizModel
     */
    public void onFinallFailCallBack(P bizModel);
}
