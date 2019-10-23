package com.jidian.cosalon.migration.pos365.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class TaskConfig {

    @Bean
    @Primary
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(40);
        executor.setMaxPoolSize(40);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("HaiMT_Thread");
        executor.initialize();

        return executor;
    }
}
