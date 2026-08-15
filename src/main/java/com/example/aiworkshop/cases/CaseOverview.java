package com.example.aiworkshop.cases;

import java.util.List;

/**
 * One row of the Case Handler's list: enough to decide whether this Case is worth opening.
 *
 * <p>Nothing here costs a model call, which is the point — the list is skimmed, and a handler
 * browsing twelve Cases should not be waiting on twelve model calls.
 *
 * @param id the Case identifier
 * @param reference the human-readable name the handler recognises the Case by
 * @param status derived from the attached Documents on every read
 * @param requiredDocuments everything this Case needs before it can be decided
 * @param outstanding the subset of those that nothing has matched yet — what the Case is waiting for
 */
public record CaseOverview(
        String id, String reference, CaseStatus status, List<String> requiredDocuments, List<String> outstanding) {}
