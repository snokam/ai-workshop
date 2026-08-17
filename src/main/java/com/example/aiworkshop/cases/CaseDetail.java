package com.example.aiworkshop.cases;

import com.example.aiworkshop.document.UploadedDocument;
import com.example.aiworkshop.fraud.FraudScreening;
import java.util.List;

/**
 * One Case, opened: everything the handler screen shows for it.
 *
 * <p>Unlike {@link CaseOverview}, building this costs two model calls — which is why it is only ever
 * built for the one Case a handler actually asked for.
 *
 * @param overview the same row the list shows, so the screen needs no second lookup
 * @param documents every Document attached, including ones a later upload has superseded
 * @param countingDocumentIds which of those the status was derived from — the newest match for each
 *     Required Document. A Document that is attached but not here either lost to a newer upload of
 *     the same Required Document or matched none of them; the screen can tell those two apart by
 *     whether the Document matched anything at all
 * @param blockedDocumentIds the Documents holding this Case at {@code NEEDS_REVIEW} — the only ones a
 *     Review would change anything for. Always a subset of {@code countingDocumentIds}
 * @param summary the Case Summary, written across all the Documents
 * @param statusNote where the Case stands and the next move, written over the derived facts
 * @param screenings what the fraud checks found on these Documents
 */
public record CaseDetail(
        CaseOverview overview,
        List<UploadedDocument> documents,
        List<String> countingDocumentIds,
        List<String> blockedDocumentIds,
        String summary,
        String statusNote,
        List<FraudScreening> screenings) {}
