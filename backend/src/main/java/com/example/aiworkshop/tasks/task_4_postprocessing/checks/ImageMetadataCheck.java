package com.example.aiworkshop.tasks.task_4_postprocessing.checks;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.example.aiworkshop.tasks.task_4_postprocessing.FraudScreener.Upload;
import com.example.aiworkshop.tasks.task_4_postprocessing.model.FraudScreening.Indicator;
import com.example.aiworkshop.tasks.task_4_postprocessing.model.FraudScreening.Kind;
import com.example.aiworkshop.tasks.task_4_postprocessing.model.FraudScreening.Weight;
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
public class ImageMetadataCheck implements FraudCheck {

    private static final List<String> EDITORS =
            List.of("photoshop", "gimp", "lightroom", "affinity", "pixelmator", "paint.net", "canva");

    private static final Duration LONG_AGO = Duration.ofDays(365);

    @Override
    public List<Indicator> screen(Upload upload) {
        // TODO — task 4. What the file says about where it came from.
        //
        // upload.bytes() is the image. EXIF can say which camera took it, when, and which editor
        // last wrote it — and its absence says something too. Every one of these has an innocent
        // explanation, so weigh them accordingly: they are worth something together, little alone.
        //
        // Returning nothing is what no check looks like.
        return List.of();
    }

    private static Optional<Indicator> editedInSoftware(ExifIFD0Directory exif) {
        String software = exif == null ? null : exif.getString(ExifIFD0Directory.TAG_SOFTWARE);
        if (software == null || EDITORS.stream().noneMatch(software.toLowerCase(Locale.ROOT)::contains)) {
            return Optional.empty();
        }
        return Optional.of(new Indicator(
                Kind.EDITED_IN_SOFTWARE,
                Weight.CONCERN,
                "The image says it was last written by image editing software, not by a camera.",
                List.of("Software: " + software)));
    }

    private static Optional<Indicator> captureDate(ExifSubIFDDirectory sub) {
        Date taken = sub == null ? null : sub.getDateOriginal();
        if (taken == null) {
            return Optional.empty();
        }
        Instant capturedAt = taken.toInstant();
        Instant now = Instant.now();
        if (capturedAt.isAfter(now)) {
            return Optional.of(new Indicator(
                    Kind.DATE_OUT_OF_PLACE,
                    Weight.STRONG,
                    "The image claims to have been taken in the future.",
                    List.of("Taken: " + capturedAt)));
        }
        if (capturedAt.isBefore(now.minus(LONG_AGO))) {
            return Optional.of(new Indicator(
                    Kind.DATE_OUT_OF_PLACE,
                    Weight.NOTE,
                    "The image was taken more than a year before it was uploaded.",
                    List.of("Taken: " + capturedAt)));
        }
        return Optional.empty();
    }

    private static boolean noCameraOrigin(ExifIFD0Directory exif) {
        return exif == null
                || (exif.getString(ExifIFD0Directory.TAG_MAKE) == null
                        && exif.getString(ExifIFD0Directory.TAG_MODEL) == null);
    }
}
