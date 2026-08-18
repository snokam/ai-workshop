package com.example.aiworkshop.cases.model;

import com.example.aiworkshop.document.model.MatchConfidence;
import java.util.List;

public record CreatedCase(
        String id,
        String reference,
        String typeLabel,
        MatchConfidence confidence,
        String rationale,
        List<String> requiredDocuments,
        CaseStatus status) {}
