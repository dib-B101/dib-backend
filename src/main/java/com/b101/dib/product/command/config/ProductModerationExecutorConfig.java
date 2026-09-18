package com.b101.dib.product.command.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

// 검수는 AI 응답을 기다리는 HTTP 대기다. 등록 요청 스레드에서 떼어내려고 전용 풀을 둔다
@Configuration
public class ProductModerationExecutorConfig {

    public static final String EXECUTOR_BEAN = "productModerationExecutor";

    @Bean(name = EXECUTOR_BEAN)
    public Executor productModerationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        // 큐가 가득 차면 작업을 거절한다. 거절된 상품은 PENDING 에 남아 관리자가 처리하므로 등록 스레드를 붙잡는 것보다 낫다
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("product-moderation-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(20);
        executor.initialize();
        return executor;
    }
}
