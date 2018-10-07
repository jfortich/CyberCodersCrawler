import com.jasminefortich.crawler.utils.JsonUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.*;
import java.net.URL;

public class JsonUtilTests {

    @Test
    public void validJson() throws IOException, JSONException {
        String validJsonString = "{ \"links\": [ \"https://httpbin.org/status/200\", \"https://httpbin.org/links/1\" ] }";

        URL url = Mockito.mock(URL.class);
        InputStream is = new ByteArrayInputStream( validJsonString.getBytes() );
        Mockito.when(url.openStream()).thenReturn(is);

        JSONObject object = JsonUtil.readJsonFromUrl(url);

        Assert.assertNotNull(object);
    }

    @Test(expected = JSONException.class)
    public void invalidJson() throws IOException, JSONException {
        String invalidJsonString = "{ \"links\": } }";

        URL url = Mockito.mock(URL.class);
        InputStream is = new ByteArrayInputStream( invalidJsonString.getBytes() );
        Mockito.when(url.openStream()).thenReturn(is);

        JSONObject object = JsonUtil.readJsonFromUrl(url);

        Assert.assertNull(object);
    }

    @Test(expected = IOException.class)
    public void ioException() throws IOException, JSONException {
        String invalidJsonString = "{ \"links\": } }";

        URL url = Mockito.mock(URL.class);
        InputStream is = new ByteArrayInputStream( invalidJsonString.getBytes() );
        Mockito.when(url.openStream()).thenThrow(IOException.class);

        JSONObject object = JsonUtil.readJsonFromUrl(url);

        Assert.assertNull(object);
    }

}
