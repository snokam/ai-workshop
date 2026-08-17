package com.example.aiworkshop.fraud;

import com.example.aiworkshop.document.ManipulationAttempt;
import com.example.aiworkshop.fraud.FraudIndicator.Kind;
import com.example.aiworkshop.fraud.FraudIndicator.Weight;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
class AddressedTheAgentCheck implements FraudCheck {
    @Override
    public List<FraudIndicator> screen(ScreenedFile file) {
        ManipulationAttempt attempt = file.analysis().manipulationAttempt();
        if (attempt == null || attempt.attemptedInstruction() == null) {
            return List.of();
        }

        List<String> evidence = new ArrayList<>();
        if (attempt.quote() != null) {
            evidence.add("The document says: “" + attempt.quote() + "”");
        }
        evidence.add("The agent reported this rather than acting on it, and its other findings stand.");

        return List.of(FraudIndicator.of(
                Kind.ADDRESSED_THE_AGENT,
                Weight.STRONG,
                "The document contains text aimed at the software reading it: " + attempt.attemptedInstruction(),
                evidence));
    }
}
