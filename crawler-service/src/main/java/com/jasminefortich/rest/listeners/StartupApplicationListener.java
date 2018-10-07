package com.jasminefortich.rest.listeners;

import com.jasminefortich.rest.exceptions.CrawlerException;
import com.jasminefortich.rest.services.CrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class StartupApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOG = Logger.getLogger(StartupApplicationListener.class.getSimpleName());

    @Autowired private CrawlerService crawlerService;

    @Override public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            this.crawlerService.startCrawling();
        } catch (CrawlerException e) {
            LOG.severe(e.getMessage());
        }
    }

}
