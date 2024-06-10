import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Stream;

public class Client {
    public static final String SERVICE_URL = "https://api.nasa.gov/planetary/apod?api_key=";
    public static final String API_KEY = "6uUg9VVTzNNOTX3MYxqu7VslSsUwuaLphjNoVqJD";

    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        try (CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)
                        .setSocketTimeout(5000)
                        .setRedirectsEnabled(false)
                        .build())
                .build()) {
            HttpGet request = new HttpGet(SERVICE_URL + API_KEY);
            try (CloseableHttpResponse httpResponse = httpClient.execute(request)) {
                StringReader stringReader = new StringReader(new String(httpResponse.getEntity().getContent()
                        .readAllBytes(), StandardCharsets.UTF_8
                ));
                Response resp = objectMapper.readValue(stringReader, Response.class);
                String pageUrl = resp.getUrl();
                HttpGet imgRequest = new HttpGet(pageUrl);
                httpClient.execute(imgRequest);
                List<String> lst = Stream.of(pageUrl.split("/")).toList();
                String fileName = lst.get(lst.size() - 1);
                String mediaType = resp.getMedia_type();
                URL url = new URL(pageUrl);
                InputStream in = null;
                if (mediaType.equals("image")) {
                    in = url.openStream();
                } else if (mediaType.equals("video")) {
                    in = new FileInputStream(fileName);
                }
                Files.copy(in, Paths.get(fileName), StandardCopyOption.REPLACE_EXISTING);
                in.close();
            }
        }
    }
}
