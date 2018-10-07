package com.jasminefortich.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@SpringBootApplication
public class Application {

    @Bean(name = "crawlerThreadExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(5);
        pool.setMaxPoolSize(10);
        pool.setThreadNamePrefix("AsyncCrawlerThread-");
        pool.setWaitForTasksToCompleteOnShutdown(true);
        return pool;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}