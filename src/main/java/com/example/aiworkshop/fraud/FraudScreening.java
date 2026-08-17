package com.example.aiworkshop.fraud;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

public record FraudScreening(String documentId, List<FraudIndicator> indicators, Instant screenedAt) {
    public boolean foundSomething() {
        return !indicators.isEmpty();
    }

    public FraudIndicator.Weight heaviest() {
        return indicators.stream()
                .map(FraudIndicator::weight)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }
}
