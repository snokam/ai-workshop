package com.example.aiworkshop.fraud;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.example.aiworkshop.fraud.FraudIndicator.Kind;
import com.example.aiworkshop.fraud.FraudIndicator.Weight;
import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
class ImageMetadataCheck implements FraudCheck {
    private static final List<String> EDITORS =
            List.of("photoshop", "gimp", "lightroom", "affinity", "pixelmator", "paint.net", "canva", "inkscape");

    private static final Duration LONG_AGO = Duration.ofDays(365);

    @Override
    public List<FraudIndicator> screen(ScreenedFile file) {
        if (!file.isImage()) {
            return List.of();
        }

        Metadata metadata;
        try {
            metadata = ImageMetadataReader.readMetadata(new ByteArrayInputStream(file.content()));
        } catch (Exception e) {
            return List.of();
        }

        List<FraudIndicator> found = new ArrayList<>();
        editedInSoftware(metadata).ifPresent(found::add);
        captureDate(metadata).ifPresent(found::add);
        if (file.isJpeg()) {
            noCameraOrigin(metadata).ifPresent(found::add);
        }
        return found;
    }

    private static Optional<FraudIndicator> editedInSoftware(Metadata metadata) {
        String software = string(metadata, ExifIFD0Directory.class, ExifIFD0Directory.TAG_SOFTWARE);
        if (software == null) {
            return Optional.empty();
        }
        String lowered = software.toLowerCase(Locale.ROOT);
        if (EDITORS.stream().noneMatch(lowered::contains)) {
            return Optional.empty();
        }
        return Optional.of(FraudIndicator.of(
                Kind.EDITED_IN_SOFTWARE,
                Weight.CONCERN,
                "The image says it was last written by image editing software, not by a camera.",
                List.of("Software: " + software)));
    }

    private static Optional<FraudIndicator> captureDate(Metadata metadata) {
        ExifSubIFDDirectory exif = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        Date taken = exif == null ? null : exif.getDateOriginal();
        if (taken == null) {
            return Optional.empty();
        }

        Instant capturedAt = taken.toInstant();
        Instant now = Instant.now();
        if (capturedAt.isAfter(now)) {
            return Optional.of(FraudIndicator.of(
                    Kind.DATE_OUT_OF_PLACE,
                    Weight.STRONG,
                    "The image claims to have been taken in the future.",
                    List.of("Taken: " + capturedAt)));
        }
        if (capturedAt.isBefore(now.minus(LONG_AGO))) {
            return Optional.of(FraudIndicator.of(
                    Kind.DATE_OUT_OF_PLACE,
                    Weight.NOTE,
                    "The image was taken more than a year before it was uploaded.",
                    List.of("Taken: " + capturedAt)));
        }
        return Optional.empty();
    }

    private static Optional<FraudIndicator> noCameraOrigin(Metadata metadata) {
        String make = string(metadata, ExifIFD0Directory.class, ExifIFD0Directory.TAG_MAKE);
        String model = string(metadata, ExifIFD0Directory.class, ExifIFD0Directory.TAG_MODEL);
        if (make != null || model != null) {
            return Optional.empty();
        }
        return Optional.of(FraudIndicator.of(
                Kind.NO_CAMERA_ORIGIN,
                Weight.NOTE,
                "The photo carries none of the metadata a camera writes — no make, no model, no lens.",
                List.of("Ordinary for a screenshot, a download, or anything sent through a messaging app.")));
    }

    private static <T extends Directory> String string(Metadata metadata, Class<T> directory, int tag) {
        T found = metadata.getFirstDirectoryOfType(directory);
        return found == null ? null : found.getString(tag);
    }
}
