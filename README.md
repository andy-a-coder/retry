# 分布式有状态重试组件
    * 基于rocketmq的有状态分布式消息重试
    * 支持重试策略定制（通知几次，每次的时间间隔）
    * 支持任意次数，任意长度的时间间隔配置，最小单位是秒（时间单位支持： s-秒；m-分钟；h-小时；d-天）
    * 时间配置必须带单位，否则认为是无效配置，立即重试
## 1、代码中的使用方式(通过注解使用)
* 以下简单介绍，更多完整示例参见：[https://github.com/andy-a-coder/retry-sample](https://github.com/andy-a-coder/retry-sample) 

### 1）实现RetryExecutor接口（每个重试逻辑写一个实现类），将可能重试的业务逻辑写在execute方法中，使用@Retry注解定制通知策略
```
@Component
public class OrderNoticeExecutor implements RetryExecutor<SampleModel>{
    @Override
    @Retry(retryStrategy = {"1s","7s","1m","30m","2h","1d"})
    public <R> R execute(SampleModel model) {
//        Do what you want ...
//        RestTemplate restTemplate = new RestTemplate();
//        Map<String, Object> paramMap = new HashMap<>();
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        
//        paramMap.put("code", "2000");
//        paramMap.put("status", "success");
//        paramMap.put("message", "save order success");
//        paramMap.put("orderId", model.getOrderId());
//        paramMap.put("amount", model.getAmount());
//        restTemplate.postForEntity(model.getCallbackUrl(), new HttpEntity<>(paramMap, headers), String.class);
        return "...";
    }
    /**
     * 最终重试失败的处理策略
     * @param bizModel
     */
    public void onFinallFail(SampleModel model){
        //...
    }
}

```
### 2）在你的代码中注入上边实现的RetryExecutor，调用execute方法即可
```
    @Resource // 使用@Resource注入实现类OrderNoticeExecutor
    private OrderNoticeExecutor orderNoticeExecutor; 
    
    public String saveOrder(Order order) {
        // ...
    
        SampleModel model = new SampleModel();
        model.setName("test retry");
        model.setPrice("60.01");
        // 通知订单系统
        String result = orderNoticeExecutor.execute(model);
        
        // ...
        return "...";
    }

```
## 2、集成方式
### 1）添加maven依赖(请关注maven中央仓库中的版本)
```
<dependency>
    <groupId>com.github.andy-a-coder</groupId>
    <artifactId>retry</artifactId>
    <version>${version}</version>
</dependency>
```
### 2）yml配置
```
spring:
  application:
    name: retry-test
rocketmq:
  name-server: localhost:9876
  producer:
    # 重试消息提供者的组定义
    group: retry-test-group
```

## 3、设计原理
主要原理是以RocketMq的18个延迟级别的队列为基础，通过拼接来定制任意时间间隔的延迟通知策略。
### 1）重试过程交互图(interaction.png)
![](https://github.com/andy-a-coder/retry/blob/master/interaction.png?raw=true)
### 2）消息处理流程图(flow.png)
![](https://github.com/andy-a-coder/retry/blob/master/flow.png?raw=true)
