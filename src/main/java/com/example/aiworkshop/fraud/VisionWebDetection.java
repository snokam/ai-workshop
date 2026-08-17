package com.example.aiworkshop.fraud;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "aiworkshop.fraud.reverse-image.provider", havingValue = "vision")
class VisionWebDetection implements ReverseImageLookup {
    private static final Logger log = LoggerFactory.getLogger(VisionWebDetection.class);

    private static final int MAX_PAGES = 5;

    private final RestClient http;
    private final GoogleCredentials credentials;
    private final String quotaProject;

    VisionWebDetection(FraudProperties properties) throws IOException {
        this.http = RestClient.create(properties.reverseImage().endpoint());
        this.credentials =
                GoogleCredentials.getApplicationDefault().createScoped("https://www.googleapis.com/auth/cloud-platform");
        this.quotaProject = properties.reverseImage().project();
    }

    @Override
    public Optional<WebMatches> lookup(byte[] image, String mimeType) {
        try {
            credentials.refreshIfExpired();
            JsonNode response = http.post()
                    .uri("/v1/images:annotate")
                    .header("Authorization", "Bearer " + credentials.getAccessToken().getTokenValue())
                    .header("x-goog-user-project", quotaProject)
                    .body(request(image))
                    .retrieve()
                    .body(JsonNode.class);
            return Optional.of(matchesIn(response));
        } catch (Exception e) {
            log.warn("Reverse image lookup failed; the document is screened without it", e);
            return Optional.empty();
        }
    }

    private static Map<String, Object> request(byte[] image) {
        return Map.of(
                "requests",
                List.of(Map.of(
                        "image",
                        Map.of("content", Base64.getEncoder().encodeToString(image)),
                        "features",
                        List.of(Map.of("type", "WEB_DETECTION", "maxResults", MAX_PAGES)))));
    }

    private static WebMatches matchesIn(JsonNode response) {
        JsonNode detection = response.path("responses").path(0).path("webDetection");

        List<String> pages = new ArrayList<>();
        for (JsonNode page : detection.path("pagesWithMatchingImages")) {
            if (pages.size() == MAX_PAGES) {
                break;
            }
            pages.add(page.path("url").asText());
        }

        JsonNode bestGuess = detection.path("bestGuessLabels").path(0).path("label");
        return new WebMatches(
                detection.path("fullMatchingImages").size(),
                detection.path("partialMatchingImages").size(),
                List.copyOf(pages),
                bestGuess.isMissingNode() ? null : bestGuess.asText());
    }
}
