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

/**
 * What the file says about where it came from.
 *
 * <p>A photograph taken on a phone carries EXIF: the make and model of the camera, the moment the
 * shutter opened, often the lens. A photograph that has been through an image editor usually carries
 * the editor's name as well. Neither fact is conclusive and this class is careful not to pretend
 * otherwise — every route by which an honest photo loses or gains these tags is common:
 *
 * <ul>
 *   <li>Messaging apps and social networks strip EXIF from everything that passes through them.
 *   <li>A screenshot of a document has no camera metadata and never did.
 *   <li>Cropping a photo to remove a bystander writes an editor's name into it.
 * </ul>
 *
 * <p>So the weights here are low by design. These are the Indicators that earn their place by
 * accumulating — an image with no camera origin, edited in Photoshop, whose capture date is a year
 * before the incident is a different proposition from any one of those alone.
 */
@Component
class ImageMetadataCheck implements FraudCheck {

    /** Names that appear in the Software tag of files that have been through an editor. */
    private static final List<String> EDITORS =
            List.of("photoshop", "gimp", "lightroom", "affinity", "pixelmator", "paint.net", "canva", "inkscape");

    /** Older than this before the upload and the date is worth a handler noticing. */
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
            // An unreadable header says nothing about honesty; the quality assessment is where an
            // unusable file gets reported.
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

    /**
     * A capture date after the upload is a clock that has been moved, which is not something an
     * honest file does. A capture date long before it is ordinary for old paperwork and merely worth
     * seeing next to what the Case is about.
     */
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
