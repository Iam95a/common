package com.chen.common.utils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.Executor;

/**
 * @author ch
 */
public class AsyncTask {

    private static Executor executor;
    private static Scheduler schedulerExecutor;

    public static Scheduler getSchedulerExecutor() {

        if (schedulerExecutor == null) {
            if (executor == null) {
                return Schedulers.parallel();
            } else {
                schedulerExecutor = Schedulers.fromExecutor(executor);
                return schedulerExecutor;
            }
        }
        return schedulerExecutor;


    }

    public static void setExecutor(Executor executor) {
        AsyncTask.executor = executor;
    }

    /**
     * 默认阻塞获取的等待时长
     */
    private static final Long DEFAULT_BLOCKED_TIMEOUT = 15L;

    /**
     * 异步阻塞执行, 适合IO类型
     *
     * @param flux
     */
    public static void elasticBlockedExecute(Flux<Runnable> flux) {
        elasticBlockedExecute(flux, DEFAULT_BLOCKED_TIMEOUT);
    }

    /**
     * 异步阻塞执行, 适合IO类型
     *
     * @param flux
     * @param timeoutSeconds
     */
    public static void elasticBlockedExecute(Flux<Runnable> flux, Long timeoutSeconds) {
        blockedExecute(flux, timeoutSeconds,getSchedulerExecutor());
    }

    /**
     * 异步非阻塞执行, 适合IO类型
     *
     * @param flux
     */
    public static void elasticExecute(Flux<Runnable> flux) {
        execute(flux, getSchedulerExecutor());
    }

    /**
     * 异步阻塞执行, 适合IO类型
     *
     * @param flux
     */
    public static void parallelBlockedExecute(Flux<Runnable> flux) {
        parallelBlockedExecute(flux, DEFAULT_BLOCKED_TIMEOUT);
    }

    /**
     * 异步阻塞执行, 适合计算类型
     *
     * @param flux
     * @param timeoutSeconds
     */
    public static void parallelBlockedExecute(Flux<Runnable> flux, Long timeoutSeconds) {
        blockedExecute(flux, timeoutSeconds, Schedulers.parallel());
    }

    /**
     * 异步非阻塞执行, 适合计算类型
     *
     * @param flux
     */
    public static void parallelExecute(Flux<Runnable> flux) {
        execute(flux, Schedulers.parallel());
    }

    /**
     * 异步执行所有的Runnable， 阻塞等待最慢的一个返回
     *
     * @param flux
     * @param timeoutSeconds
     * @param scheduler
     */
    public static void blockedExecute(Flux<Runnable> flux, Long timeoutSeconds, Scheduler scheduler) {
        flux
                .flatMap(e -> Mono.fromRunnable(e).publishOn(scheduler))
                .blockLast(Duration.ofSeconds(timeoutSeconds));
    }

    /**
     * 异步执行所有的Runnable，非阻塞执行
     *
     * @param flux
     * @param scheduler
     */
    public static void execute(Flux<Runnable> flux, Scheduler scheduler) {
        flux
                .flatMap(e -> Mono.fromRunnable(e).publishOn(scheduler))
                .subscribe();
    }
}
