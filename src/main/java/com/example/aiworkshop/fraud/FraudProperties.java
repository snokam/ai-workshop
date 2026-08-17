package com.example.aiworkshop.fraud;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "aiworkshop.fraud")
public record FraudProperties(@DefaultValue ReverseImage reverseImage) {
    public record ReverseImage(
            @DefaultValue("none") String provider,
            @DefaultValue("unspecified") String project,
            @DefaultValue("https://vision.googleapis.com") String endpoint) {}
}
