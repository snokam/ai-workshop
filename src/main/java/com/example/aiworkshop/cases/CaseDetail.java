package com.example.aiworkshop.cases;

import com.example.aiworkshop.document.UploadedDocument;
import java.util.List;

/**
 * One Case, opened: everything the handler screen shows for it.
 *
 * <p>Unlike {@link CaseOverview}, building this costs two model calls — which is why it is only ever
 * built for the one Case a handler actually asked for.
 *
 * @param overview the same row the list shows, so the screen needs no second lookup
 * @param documents every Document attached, including ones a later upload has superseded
 * @param summary the Case Summary, written across all the Documents
 * @param statusNote where the Case stands and the next move, written over the derived facts
 */
public record CaseDetail(
        CaseOverview overview, List<UploadedDocument> documents, String summary, String statusNote) {}
