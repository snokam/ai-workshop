package com.example.aiworkshop.fraud;

import com.example.aiworkshop.fraud.FraudIndicator.Kind;
import com.example.aiworkshop.fraud.FraudIndicator.Weight;
import com.example.aiworkshop.fraud.ReverseImageLookup.WebMatches;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
class ReverseImageCheck implements FraudCheck {
    private final ReverseImageLookup lookup;

    ReverseImageCheck(ReverseImageLookup lookup) {
        this.lookup = lookup;
    }

    @Override
    public List<FraudIndicator> screen(ScreenedFile file) {
        if (!file.isImage()) {
            return List.of();
        }

        Optional<WebMatches> found = lookup.lookup(file.content(), file.contentType());
        if (found.isEmpty() || !found.get().anywhere()) {
            return List.of();
        }

        WebMatches matches = found.get();
        return List.of(FraudIndicator.of(
                Kind.SEEN_ONLINE,
                matches.fullMatches() > 0 ? Weight.STRONG : Weight.CONCERN,
                describe(matches),
                evidence(matches)));
    }

    private static String describe(WebMatches matches) {
        if (matches.fullMatches() > 0) {
            return "This exact image is published online, on %d page(s) the search could name."
                    .formatted(matches.fullMatches());
        }
        return "A cropped or edited version of this image appears online, in %d place(s)."
                .formatted(matches.partialMatches());
    }

    private static List<String> evidence(WebMatches matches) {
        List<String> evidence = new ArrayList<>(matches.pages());
        if (matches.bestGuessLabel() != null) {
            evidence.add("The search reads the picture as: " + matches.bestGuessLabel());
        }
        return evidence;
    }
}
