package com.example.aiworkshop.cases;

import com.example.aiworkshop.document.MatchConfidence;
import java.util.List;

public record CreatedCase(
        String id,
        String reference,
        String typeLabel,
        MatchConfidence confidence,
        String rationale,
        List<String> requiredDocuments,
        CaseStatus status) {}
