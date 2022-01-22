package com.chen;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;

/**
 * @author goldgreat
 * @Date 2021-11-24
 */
public class HelloCommand extends HystrixCommand<String> {
    protected HelloCommand() {


        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("test"))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        //开启熔断模式
                        .withCircuitBreakerEnabled(true)
                        //出现错误的比率超过30%就开启熔断
                        .withCircuitBreakerErrorThresholdPercentage(30)
                        //至少有10个请求才进行errorThresholdPercentage错误百分比计算
                        .withCircuitBreakerRequestVolumeThreshold(10)
                        //半开试探休眠时间，这里设置为3秒
                        .withCircuitBreakerSleepWindowInMilliseconds(3000)
                )
        );

    }

    @Override
    protected String run() throws Exception {
        //模拟外部请求需要的时间长度
        System.out.println("执行了run方法");
        Thread.sleep(2000);
        return "sucess";
    }

    @Override
    protected String getFallback() {
        //当外部请求超时后，会执行fallback里的业务逻辑
        System.out.println("执行了回退方法");
        return "error";
    }


}
