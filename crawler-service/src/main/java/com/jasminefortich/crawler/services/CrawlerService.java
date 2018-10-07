package com.jasminefortich.crawler.services;

import com.google.gson.Gson;
import com.jasminefortich.crawler.exceptions.CrawlerException;
import com.jasminefortich.crawler.models.StartEndpoint;
import com.jasminefortich.crawler.utils.JsonUtil;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

@Service
public class CrawlerService {

    private static final Logger LOGGER = Logger.getLogger(CrawlerService.class.getSimpleName());

    private volatile Set<String> VISITED_LINKS = Collections.synchronizedSet(new HashSet<>());
    private volatile Queue<String> LINK_QUEUE  = new LinkedList<>();

    private List<CrawlerThread> CRAWLER_THREADS = new ArrayList<>();

    private Integer REQUEST_COUNT = 0, SUCCESS_COUNT = 0, FAILED_COUNT = 0;

    @Value("${crawler.start-endpoint}")
    private String startEndpoint;

    @Autowired
    ThreadPoolTaskExecutor threadPool;

    /**
     * Sets the starting endpoint
     *
     * @param endpoint The new start endpoint
     */
    public void setStartEndpoint(String endpoint) {
        this.startEndpoint = endpoint;
    }

    /**
     * Resets the crawler service by clearing the 'visited' and 'to visit' lists
     */
    public void resetCrawlerService() {
        LINK_QUEUE.clear();
        VISITED_LINKS.clear();
        resetCrawlerStatistics();
    }

    /**
     * Resets the crawler statistic counts to 0
     */
    private void resetCrawlerStatistics() {
        REQUEST_COUNT = 0;
        SUCCESS_COUNT = 0;
        FAILED_COUNT  = 0;
    }

    /**
     * Starts crawling a URL recursively and gathers statistics from the hit sites
     *
     * @throws CrawlerException Thrown if crawler endpoint is not set or the endpoint is not valid
     */
    public void startCrawling() throws CrawlerException {
        Instant start = Instant.now();
        resetCrawlerService();

        LOGGER.info("Crawler started: " + this.startEndpoint);

        if (this.startEndpoint == null || this.startEndpoint.isEmpty()) {
            throw new CrawlerException("Crawler endpoint is not set!");
        }

        JSONObject json;
        try {
            URL url  = new URL(this.startEndpoint);
            json = JsonUtil.readJsonFromUrl(url);
        } catch (MalformedURLException e) {
            throw new CrawlerException("Invalid starting endpoint '" + this.startEndpoint + "'");
        } catch (IOException e) {
            throw new CrawlerException("Could not crawl starting endpoint", e);
        }

        if (json == null){
            throw new CrawlerException("Crawler endpoint is not expected structure");
        }

        Gson gson = new Gson();
        StartEndpoint startEndpoint = gson.fromJson(json.toString(), StartEndpoint.class);

        if (startEndpoint != null) {
            LINK_QUEUE.addAll(startEndpoint.getLinks());
        }

        // While there are still links to visit, let's crawl
        while (LINK_QUEUE.size() > 0 || CRAWLER_THREADS.size() > 0) {
            String link = LINK_QUEUE.poll();
            if (link != null) {
                // If we haven't visited this link, let's crawl it
                if (!VISITED_LINKS.contains(link)) {
                    crawlSite(link);
                    VISITED_LINKS.add(link);
                } else {
                    LOGGER.info("Skipping " + link + "...");
                }
            }
        }

        Instant end = Instant.now();
        LOGGER.info("Finished crawling in " + Duration.between(start, end).getSeconds() + " seconds");
        printCrawlSummary();

    }

    /**
     * Crawls a site and queues any child links to the crawler service
     * @param link The link to crawl
     */
    private void crawlSite(String link) {
        CrawlerThread thread = new CrawlerThread(link);
        threadPool.execute(thread);
        CRAWLER_THREADS.add(thread);
    }

    /**
     * Adds a link to the queue
     * @param link The link to queue
     */
    private synchronized void enqueueLink(String link) {
        if (!VISITED_LINKS.contains(link)) {
            LINK_QUEUE.add(link);
        }
    }

    /**
     * Adds a link to the visited list
     *
     * @param link The link to add
     */
    private synchronized void addVisitedLink(String link) {
        VISITED_LINKS.add(link);
    }

    /**
     * Determines whether an http response code is successful or not.
     * 200 OK, 201 OK are considered successful
     * 400 BAD REQUEST, 404 NOT FOUND, 502 BAD GATEWAY, 500 INTERNAL SERVER ERROR are considered failures
     *
     * @param code The connection response status code
     * @return True if status is considered successful, else false
     */
    private Boolean isSuccessfulRequest(int code) {
        return code < 400;
    }

    /**
     * Increments the number of requests and success count
     */
    private synchronized void logSuccessfulResponse() {
        REQUEST_COUNT++;
        SUCCESS_COUNT++;
    }

    /**
     * Increments the number of requests and failed count
     */
    private synchronized void logFailedResponse() {
        REQUEST_COUNT++;
        FAILED_COUNT++;
    }

    /**
     * Prints the crawl summary
     */
    private void printCrawlSummary() {
        LOGGER.info("Total requests: " + REQUEST_COUNT);
        LOGGER.info("Success count : " + SUCCESS_COUNT);
        LOGGER.info("Failed count  : " + FAILED_COUNT);
    }

    /**
     * Crawler Thread class
     */
    private class CrawlerThread extends Thread {
        private String link;

        public CrawlerThread() { super(); }

        public CrawlerThread(String link) {
            super();
            this.link = link;
        }

        @Override
        public void run() {
            try {
                LOGGER.info("Crawling " + link);

                Connection connection = Jsoup.connect(link);

                Connection.Response siteResponse = connection.execute();

                int responseCode = siteResponse.statusCode();
                if (isSuccessfulRequest(responseCode)) {
                    logSuccessfulResponse();

                    Document site = connection.get();

                    Elements siteLinkTags = site.select("a[href]");
                    siteLinkTags.forEach(element -> {
                        String siteLink = element.absUrl("href");
                        enqueueLink(siteLink);
                    });
                } else {
                    logFailedResponse();
                }

            } catch (IOException e) {
                logFailedResponse();
            }

            addVisitedLink(link);

            CRAWLER_THREADS.remove(this);
        }
    }

}
