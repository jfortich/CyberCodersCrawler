package com.jasminefortich.rest.controllers;

import com.jasminefortich.rest.exceptions.CrawlerException;
import com.jasminefortich.rest.models.CrawlRequest;
import com.jasminefortich.rest.services.CrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@Controller("/api/crawler")
public class CrawlerController {

    @Autowired private CrawlerService crawlerService;

    @PostMapping("/start")
    public ResponseEntity startCrawler(@RequestBody @Valid CrawlRequest crawlRequest) throws CrawlerException {
        String startEndpoint = crawlRequest.getStartEndpoint();

        if (startEndpoint == null || startEndpoint.isEmpty()) {
            throw new CrawlerException("Starting endpoint must not be empty");
        }

        URI startUri;
        try {
            URL url  = new URL(startEndpoint);
            startUri = url.toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new CrawlerException("Invalid starting endpoint '" + startEndpoint + "'");
        }

        return ResponseEntity.ok("OK");
    }


}
