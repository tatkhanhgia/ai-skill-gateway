package com.skillgateway.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "search")
public interface AppConfig {
    Weights weight();
    int defaultLimit();
    int maxLimit();

    interface Weights {
        double keyword();
        double semantic();
        double popularity();
    }
}
