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

/**
 * Reverse image search through Cloud Vision's Web Detection feature.
 *
 * <p>Active only when {@code aiworkshop.fraud.reverse-image.provider=vision}. Off by default: it is
 * the one thing in this application that sends an uploaded file to a service other than the model
 * provider, and that should be a decision someone makes rather than a default they inherit.
 *
 * <p>Authenticates with the same Application Default Credentials as the Vertex provider — no key,
 * no second account. It does need the API switched on for the project, once:
 *
 * <pre>{@code
 * gcloud services enable vision.googleapis.com --project <project>
 * }</pre>
 *
 * <p>The first thousand images a month are free, which is more than a workshop will ever use, and
 * the quota project on the credentials is what gets billed after that.
 *
 * <p>Failure is always {@link Optional#empty()}: a wrong answer here would put a fraud Indicator on
 * an honest Claimant's document, and an outage should not.
 */
@Component
@ConditionalOnProperty(name = "aiworkshop.fraud.reverse-image.provider", havingValue = "vision")
class VisionWebDetection implements ReverseImageLookup {

    private static final Logger log = LoggerFactory.getLogger(VisionWebDetection.class);

    /** Enough pages for a handler to judge; the count, not the list, is what carries the weight. */
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
            // Deliberately broad: a lookup is an opinion about a document, and no opinion is safer
            // than one produced by a half-failed call.
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

    /**
     * Vision returns matches in three lists. Full matches are the same image; partial matches are
     * crops and re-encodings, which is what someone who lifted a photo and cropped the watermark
     * off leaves behind; pages are where a handler can go and see for themselves.
     */
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
