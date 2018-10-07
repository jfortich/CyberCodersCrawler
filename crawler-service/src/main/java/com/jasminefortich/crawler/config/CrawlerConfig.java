package com.jasminefortich.crawler.config;

import com.jasminefortich.crawler.exceptions.CrawlerException;
import com.jasminefortich.crawler.services.CrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
@Configuration
public class CrawlerConfig {

    @Autowired
    private CrawlerService crawlerService;

    @Value("${crawler.start-endpoint}")
    private String startEndpoint;

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
        crawlerService.crawlEndpoint(startEndpoint);
    }

}
