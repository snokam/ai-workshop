package com.example.aiworkshop.fraud;

import java.util.List;
import java.util.Optional;

/**
 * Asks the web whether it has seen this image before.
 *
 * <p>An interface because the answer comes from outside and the outside is replaceable: Cloud Vision
 * today, TinEye or a self-hosted index tomorrow, nothing at all when the workshop is offline. What
 * the rest of the package needs is {@link WebMatches}, not a vendor.
 *
 * <p>{@link Optional#empty()} means <em>not answered</em> — the lookup is switched off, the call
 * failed, the file is a PDF. It never means "no matches". Those are different facts and a Case
 * Handler must not have them merged: no matches is mild evidence the photo is the Claimant's own,
 * while a lookup that did not run is no evidence about anything.
 */
public interface ReverseImageLookup {

    Optional<WebMatches> lookup(byte[] image, String mimeType);

    /**
     * Where the image turned up.
     *
     * @param fullMatches how many copies of this exact image the provider found published
     * @param partialMatches copies that have been cropped, resized or edited — the interesting number
     *     when someone has tried to disguise a lifted photo
     * @param pages the pages carrying it, capped to a handful, so a handler can go and look
     * @param bestGuessLabel what the provider thinks the picture is of, or {@code null}. Occasionally
     *     the whole story on its own: "2019 Volkswagen Golf for sale"
     */
    record WebMatches(int fullMatches, int partialMatches, List<String> pages, String bestGuessLabel) {

        public boolean anywhere() {
            return fullMatches > 0 || partialMatches > 0;
        }
    }
}
