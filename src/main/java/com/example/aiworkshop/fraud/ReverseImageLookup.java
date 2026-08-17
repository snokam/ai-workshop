package com.example.aiworkshop.fraud;

import java.util.List;
import java.util.Optional;

public interface ReverseImageLookup {
    Optional<WebMatches> lookup(byte[] image, String mimeType);

    record WebMatches(int fullMatches, int partialMatches, List<String> pages, String bestGuessLabel) {
        public boolean anywhere() {
            return fullMatches > 0 || partialMatches > 0;
        }
    }
}
