package com.example.aiworkshop.fraud;

import java.util.List;

public record FraudIndicator(Kind kind, Weight weight, String detail, List<String> evidence) {
    public static FraudIndicator of(Kind kind, Weight weight, String detail, List<String> evidence) {
        return new FraudIndicator(kind, weight, detail, List.copyOf(evidence));
    }

    public enum Kind {
        SEEN_ONLINE,
        ALREADY_UPLOADED,
        EDITED_IN_SOFTWARE,
        NO_CAMERA_ORIGIN,
        DATE_OUT_OF_PLACE,
        ADDRESSED_THE_AGENT
    }

    public enum Weight {
        NOTE,
        CONCERN,
        STRONG
    }
}
