package com.zcy.forum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 自定义异步线程池配置（专用于会话历史入库、通知推送等异步任务）
 */
@Configuration
public class AsyncThreadPoolConfig {

    /**
     * 会话历史入库专用线程池
     * 命名规则：chat-history-线程编号
     */
    @Bean
    public ThreadPoolTaskExecutor chatHistoryExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 1. 核心线程数：CPU核心数*2（适配大多数服务器，可根据业务调整）
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
        executor.setCorePoolSize(corePoolSize);

        // 2. 最大线程数：核心线程数*4（应对突发高并发）
        executor.setMaxPoolSize(corePoolSize * 4);

        // 3. 任务队列容量：1000（核心线程忙时，任务先入队列）
        executor.setQueueCapacity(1000);

        // 4. 线程名前缀：便于日志排查（如 chat-history-1、chat-history-2）
        executor.setThreadNamePrefix("chat-history-");

        // 5. 空闲线程存活时间：60秒（核心线程外的空闲线程，60秒后回收）
        executor.setKeepAliveSeconds((int) TimeUnit.SECONDS.toSeconds(60));

        // 6. 拒绝策略：核心+队列满时，由调用线程执行（避免任务丢失）
        // 场景：高并发下线程池满了，会在主线程中执行入库，保证数据不丢（牺牲一点性能换可靠性）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 7. 等待所有任务执行完再关闭（应用停机时）
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 8. 停机等待时间：30秒（超过则强制关闭）
        executor.setAwaitTerminationSeconds(30);

        // 初始化线程池
        executor.initialize();
        return executor;
    }

    /**
     * 可选：系统通知推送专用线程池（如果有其他异步任务，可单独配置）
     */

}