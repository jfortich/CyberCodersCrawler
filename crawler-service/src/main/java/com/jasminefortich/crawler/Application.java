package com.jasminefortich.crawler;

import com.jasminefortich.crawler.exceptions.CrawlerException;
import com.jasminefortich.crawler.services.CrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
public class Application {

    @Autowired private CrawlerService crawlerService;

    @Bean(name = "crawlerThreadExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(5);
        pool.setMaxPoolSize(10);
        pool.setThreadNamePrefix("AsyncCrawlerThread-");
        pool.setWaitForTasksToCompleteOnShutdown(true);
        return pool;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startCrawler() throws CrawlerException {
        crawlerService.startCrawling();
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}