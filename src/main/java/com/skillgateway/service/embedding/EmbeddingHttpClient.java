package com.skillgateway.service.embedding;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Map;

public interface EmbeddingHttpClient {

    EmbeddingHttpResponse post(URI uri, Duration timeout, Map<String, String> headers, String body)
        throws IOException, InterruptedException;
}
