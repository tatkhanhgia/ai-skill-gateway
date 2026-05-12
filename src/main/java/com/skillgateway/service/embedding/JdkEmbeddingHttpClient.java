package com.skillgateway.service.embedding;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class JdkEmbeddingHttpClient implements EmbeddingHttpClient {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public EmbeddingHttpResponse post(URI uri, Duration timeout, Map<String, String> headers, String body)
        throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(uri)
            .version(HttpClient.Version.HTTP_1_1)
            .timeout(timeout)
            .POST(HttpRequest.BodyPublishers.ofString(body));

        headers.forEach(builder::header);

        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        return new EmbeddingHttpResponse(response.statusCode(), response.body());
    }
}
