package com.jasminefortich.crawler.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

public class JsonUtil {

    /**
     * Reads JSON data from the given URL
     *
     * @param url The URL to download the json from
     * @return The JSON object
     * @throws IOException Thrown if error opening the url stream or reading the content
     */
    public static JSONObject readJsonFromUrl(URL url) throws IOException, JSONException {
        try (InputStream inputStream = url.openStream()) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String jsonString = readContent(bufferedReader);
            if (!jsonString.isEmpty()) {
                return new JSONObject(jsonString);
            }

        }
        return null;
    }

    /**
     * Reads string content from a buffered reader
     *
     * @param reader The buffered reader
     * @return The entire string contents of the reader
     * @throws IOException Thrown if error reading input stream
     */
    private static String readContent(BufferedReader reader) throws IOException {
        StringBuilder content = new StringBuilder();
        int line;
        while ((line = reader.read()) != -1) {
            content.append((char) line);
        }
        return content.toString();
    }

}
