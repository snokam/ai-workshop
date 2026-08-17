package com.example.aiworkshop.fraud;

import com.example.aiworkshop.fraud.FraudIndicator.Kind;
import com.example.aiworkshop.fraud.FraudIndicator.Weight;
import com.example.aiworkshop.fraud.ReverseImageLookup.WebMatches;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Has this picture been published before?
 *
 * <p>The check the whole package was asked for, and the one with the clearest story: a photo of
 * damage that already exists on a used-car listing was not taken by the person claiming for it. The
 * same question catches a licence lifted from an image search and a receipt downloaded off a
 * supplier's website.
 *
 * <p>Only ever runs on images. A PDF has no reverse image search — and a PDF of a scanned document
 * is the normal way honest paperwork arrives, so silence here is not a signal about it.
 *
 * <p>Weighting is deliberately asymmetric. A full match is hard to explain: these exact bytes are
 * already on the web. A partial match is weaker on its own — thumbnails, re-encodings and the
 * provider's own crops all produce partial matches — but it is exactly what cropping a watermark
 * off leaves behind, so it is worth a handler's eyes rather than nothing.
 */
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
            // Nothing found and not looked at all deliberately produce the same silence here. The
            // difference between them is not a fact about this document, and a Case Handler reading
            // "no matches" as reassurance would be reading something the lookup cannot promise.
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
