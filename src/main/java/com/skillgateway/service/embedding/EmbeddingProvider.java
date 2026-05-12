package com.skillgateway.service.embedding;

public interface EmbeddingProvider {

    float[] embed(String text);
}
