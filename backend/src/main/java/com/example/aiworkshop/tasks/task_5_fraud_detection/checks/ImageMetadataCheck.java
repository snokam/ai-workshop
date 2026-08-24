package com.example.aiworkshop.tasks.task_5_fraud_detection.checks;

import com.example.aiworkshop.workshop.WorkshopTask;
import com.example.aiworkshop.workshop.TaskNotImplementedException;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.example.aiworkshop.tasks.task_5_fraud_detection.FraudScreener.Upload;
import com.example.aiworkshop.tasks.task_5_fraud_detection.model.FraudScreening.Indicator;
import com.example.aiworkshop.tasks.task_5_fraud_detection.model.FraudScreening.Kind;
import com.example.aiworkshop.tasks.task_5_fraud_detection.model.FraudScreening.Weight;
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
        // TODO — task 5, part 2. What the file says about where it came from.
        //
        // EXIF, read from bytes you already have. No model, no network — that is the point of this task.
        //
        //   upload.isImage()  whether there is anything to read
        //   upload.bytes()    the file itself
        //
        // Worth flagging: software that says the image has been through an editor, and a capture date that
        // sits oddly against the case. Kind.EDITED_IMAGE and Kind.DATE_OUT_OF_PLACE.
        //
        // Metadata is missing far more often than it is damning — a screenshot has none, and most messaging
        // apps strip it. Absence is not evidence.

        throw new TaskNotImplementedException(WorkshopTask.FRAUD_DETECTION);
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
