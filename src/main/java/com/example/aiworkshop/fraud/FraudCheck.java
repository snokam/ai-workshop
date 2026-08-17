package com.example.aiworkshop.fraud;

import java.util.List;

public interface FraudCheck {
    List<FraudIndicator> screen(ScreenedFile file);

    default String name() {
        return getClass().getSimpleName();
    }
}
