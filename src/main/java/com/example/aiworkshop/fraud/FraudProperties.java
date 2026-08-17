package com.example.aiworkshop.fraud;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration for the fraud checks.
 *
 * <p>Only the reverse image lookup has any: the other checks read the file that is already in hand
 * and cost nothing, so there is nothing to turn off and no reason to.
 *
 * @param reverseImage which service, if any, answers "has this picture been published before"
 */
@ConfigurationProperties(prefix = "aiworkshop.fraud")
public record FraudProperties(@DefaultValue ReverseImage reverseImage) {

    /**
     * @param provider {@code none} (the default — the lookup reports that it did not run) or
     *     {@code vision} for Cloud Vision Web Detection
     * @param project the Google Cloud project billed for the lookup, and the one the API has to be
     *     enabled on. Defaults to the project the Vertex provider uses
     * @param endpoint the Vision base URL, here so a test or a proxy can point it elsewhere
     */
    public record ReverseImage(
            @DefaultValue("none") String provider,
            @DefaultValue("unspecified") String project,
            @DefaultValue("https://vision.googleapis.com") String endpoint) {}
}
