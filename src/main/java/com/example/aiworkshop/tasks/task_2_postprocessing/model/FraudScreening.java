package com.example.aiworkshop.tasks.task_2_postprocessing.model;

import java.util.List;

public record FraudScreening(String documentId, List<Indicator> indicators) {

    public boolean foundSomething() {
        return !indicators.isEmpty();
    }

    public record Indicator(Kind kind, Weight weight, String detail, List<String> evidence) {}

    public enum Kind {
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
