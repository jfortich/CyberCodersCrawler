import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jasminefortich.crawler.exceptions.CrawlerException;
import com.jasminefortich.crawler.services.CrawlerService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

public class CrawlerServiceTest {

    @InjectMocks
    private CrawlerService crawlerService;

    @Rule
    public ExpectedException failure = ExpectedException.none();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);

    private final String MOCK_ENDPOINT = "http://localhost:8089/start-endpoint";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ThreadPoolTaskExecutor threadPoolTaskExecutor = createThreadPoolTaskExecutor();
        threadPoolTaskExecutor.initialize();

        Field executorField = ReflectionUtils.findField(CrawlerService.class, "threadPool");
        ReflectionUtils.makeAccessible(executorField);
        ReflectionUtils.setField(executorField, crawlerService, threadPoolTaskExecutor);
    }

    /**
     * Creates the thread pool task executor for the crawler service
     *
     * @return ThreadPoolTaskExecutor
     */
    private ThreadPoolTaskExecutor createThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(5);
        pool.setMaxPoolSize(10);
        pool.setThreadNamePrefix("AsyncCrawlerThread-");
        pool.setWaitForTasksToCompleteOnShutdown(true);
        return pool;
    }

    /**
     * Creates the mock endpoint and returns the passed in json body
     *
     * @param jsonBody The json body to return
     */
    private void createCrawlEndpoint(String jsonBody) {
        stubFor(get(urlEqualTo("/start-endpoint"))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-type", "application/json")
                                .withBody(jsonBody)
                )
        );
    }

    @Test
    public void nullEndpoint() throws CrawlerException {
        failure.expect(CrawlerException.class);
        failure.expectMessage("Crawler endpoint is not set!");

        crawlerService.crawlEndpoint(null);
    }

    @Test
    public void emptyEndpoint() throws CrawlerException {
        failure.expect(CrawlerException.class);
        failure.expectMessage("Crawler endpoint is not set!");

        crawlerService.crawlEndpoint("");
    }

    @Test
    public void invalidEndpoint() throws CrawlerException {
        failure.expect(CrawlerException.class);
        failure.expectMessage("Invalid starting endpoint \"notavalidendpoint\"");

        String invalidEndpoint = "notavalidendpoint";
        crawlerService.crawlEndpoint(invalidEndpoint);
    }

    @Test
    public void crawl200Status() throws CrawlerException {
        String jsonString = "{ \"links\": [ \"https://httpbin.org/status/200\" ] }";
        createCrawlEndpoint(jsonString);

        crawlerService.crawlEndpoint(MOCK_ENDPOINT);

        Assert.assertEquals(1, crawlerService.getRequestCount().longValue());
        Assert.assertEquals(1, crawlerService.getSuccessCount().longValue());
        Assert.assertEquals(0, crawlerService.getFailedCount().longValue());
    }

    @Test
    public void crawl201Status() throws CrawlerException {
        String jsonString = "{ \"links\": [ \"https://httpbin.org/status/201\" ] }";
        createCrawlEndpoint(jsonString);

        crawlerService.crawlEndpoint(MOCK_ENDPOINT);

        Assert.assertEquals(1, crawlerService.getRequestCount().longValue());
        Assert.assertEquals(1, crawlerService.getSuccessCount().longValue());
        Assert.assertEquals(0, crawlerService.getFailedCount().longValue());
    }

    @Test
    public void crawlSuccessfulDuplicateRequests() throws CrawlerException {
        String jsonString = "{ \"links\": [ \"https://httpbin.org/status/200\", \"https://httpbin.org/status/200\", \"https://httpbin.org/status/200\" ] }";
        createCrawlEndpoint(jsonString);

        crawlerService.crawlEndpoint(MOCK_ENDPOINT);

        Assert.assertEquals(1, crawlerService.getRequestCount().longValue());
        Assert.assertEquals(1, crawlerService.getSuccessCount().longValue());
        Assert.assertEquals(0, crawlerService.getFailedCount().longValue());
    }

    @Test
    public void crawlSuccessfulUniqueRequests() throws CrawlerException {
        String jsonString = "{ \"links\": [ \"https://httpbin.org/status/200\", \"https://httpbin.org/links/1\", \"https://httpbin.org/links/2\" ] }";
        createCrawlEndpoint(jsonString);

        crawlerService.crawlEndpoint(MOCK_ENDPOINT);

        Assert.assertEquals(5, crawlerService.getRequestCount().longValue());
        Assert.assertEquals(5, crawlerService.getSuccessCount().longValue());
        Assert.assertEquals(0, crawlerService.getFailedCount().longValue());
    }

    @Test
    public void crawlSuccessfulUniqueAndDuplicateRequests() throws CrawlerException {
        String jsonString = "{ \"links\": [ \"https://httpbin.org/status/200\", \"https://httpbin.org/status/200\", \"https://httpbin.org/links/1\", \"https://httpbin.org/links/1\", \"https://httpbin.org/links/2\" ] }";
        createCrawlEndpoint(jsonString);

        crawlerService.crawlEndpoint(MOCK_ENDPOINT);

        Assert.assertEquals(5, crawlerService.getRequestCount().longValue());
        Assert.assertEquals(5, crawlerService.getSuccessCount().longValue());
        Assert.assertEquals(0, crawlerService.getFailedCount().longValue());
    }

    @Test
    public void crawl502Error() throws CrawlerException {
        String jsonString = "{ \"links\": [ \"https://httpbin.org/status/502\" ] }";
        createCrawlEndpoint(jsonString);

        crawlerService.crawlEndpoint(MOCK_ENDPOINT);

        Assert.assertEquals(1, crawlerService.getRequestCount().longValue());
        Assert.assertEquals(0, crawlerService.getSuccessCount().longValue());
        Assert.assertEquals(1, crawlerService.getFailedCount().longValue());
    }

    @Test
    public void crawl500Error() throws CrawlerException {
        String jsonString = "{ \"links\": [ \"https://httpbin.org/status/500\" ] }";
        createCrawlEndpoint(jsonString);

        crawlerService.crawlEndpoint(MOCK_ENDPOINT);

        Assert.assertEquals(1, crawlerService.getRequestCount().longValue());
        Assert.assertEquals(0, crawlerService.getSuccessCount().longValue());
        Assert.assertEquals(1, crawlerService.getFailedCount().longValue());
    }

    @Test
    public void crawl400Error() throws CrawlerException {
        String jsonString = "{ \"links\": [ \"https://httpbin.org/status/400\" ] }";
        createCrawlEndpoint(jsonString);

        crawlerService.crawlEndpoint(MOCK_ENDPOINT);

        Assert.assertEquals(1, crawlerService.getRequestCount().longValue());
        Assert.assertEquals(0, crawlerService.getSuccessCount().longValue());
        Assert.assertEquals(1, crawlerService.getFailedCount().longValue());
    }

    @Test
    public void crawl404Error() throws CrawlerException {
        String jsonString = "{ \"links\": [ \"https://httpbin.org/status/404\" ] }";
        createCrawlEndpoint(jsonString);

        crawlerService.crawlEndpoint(MOCK_ENDPOINT);

        Assert.assertEquals(1, crawlerService.getRequestCount().longValue());
        Assert.assertEquals(0, crawlerService.getSuccessCount().longValue());
        Assert.assertEquals(1, crawlerService.getFailedCount().longValue());
    }

    @Test
    public void crawlDuplicateFailedRequests() throws CrawlerException {
        String jsonString = "{ \"links\": [ \"https://httpbin.org/status/404\", \"https://httpbin.org/status/404\", \"https://httpbin.org/status/404\" ] }";
        createCrawlEndpoint(jsonString);

        crawlerService.crawlEndpoint(MOCK_ENDPOINT);

        Assert.assertEquals(1, crawlerService.getRequestCount().longValue());
        Assert.assertEquals(0, crawlerService.getSuccessCount().longValue());
        Assert.assertEquals(1, crawlerService.getFailedCount().longValue());
    }

    @Test
    public void crawlUniqueFailedRequests() throws CrawlerException {
        String jsonString = "{ \"links\": [ \"https://httpbin.org/status/404\", \"https://httpbin.org/status/400\", \"https://httpbin.org/status/500\" ] }";
        createCrawlEndpoint(jsonString);

        crawlerService.crawlEndpoint(MOCK_ENDPOINT);

        Assert.assertEquals(3, crawlerService.getRequestCount().longValue());
        Assert.assertEquals(0, crawlerService.getSuccessCount().longValue());
        Assert.assertEquals(3, crawlerService.getFailedCount().longValue());
    }

    @Test
    public void crawlUniqueAndDuplicateFailedRequests() throws CrawlerException {
        String jsonString = "{ \"links\": [ \"https://httpbin.org/status/404\", \"https://httpbin.org/status/404\", \"https://httpbin.org/status/400\", \"https://httpbin.org/status/400\", \"https://httpbin.org/status/500\" ] }";
        createCrawlEndpoint(jsonString);

        crawlerService.crawlEndpoint(MOCK_ENDPOINT);

        Assert.assertEquals(3, crawlerService.getRequestCount().longValue());
        Assert.assertEquals(0, crawlerService.getSuccessCount().longValue());
        Assert.assertEquals(3, crawlerService.getFailedCount().longValue());
    }

    @Test
    public void crawlRequestsWithChildLinks() throws CrawlerException {
        String jsonString = "{ \"links\": [ \"https://httpbin.org/links/10\" ] }";
        createCrawlEndpoint(jsonString);

        crawlerService.crawlEndpoint(MOCK_ENDPOINT);

        Assert.assertEquals(11, crawlerService.getRequestCount().longValue());
        Assert.assertEquals(11, crawlerService.getSuccessCount().longValue());
        Assert.assertEquals(0, crawlerService.getFailedCount().longValue());
    }

    @Test
    public void crawlRequestsWithChildLinksAndDuplicates() throws CrawlerException {
        String jsonString = "{ \"links\": [ \"https://httpbin.org/links/10\", \"https://httpbin.org/links/10/0\" ] }";
        createCrawlEndpoint(jsonString);

        crawlerService.crawlEndpoint(MOCK_ENDPOINT);

        Assert.assertEquals(11, crawlerService.getRequestCount().longValue());
        Assert.assertEquals(11, crawlerService.getSuccessCount().longValue());
        Assert.assertEquals(0, crawlerService.getFailedCount().longValue());
    }
}
