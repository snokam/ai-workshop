package com.example.aiworkshop.tasks.task_1_first_agent.model;

import com.example.aiworkshop.cases.model.CaseStatus;
import com.example.aiworkshop.documents.model.MatchConfidence;
import java.util.List;

public record CreatedCase(
        String id,
        String reference,
        String typeLabel,
        MatchConfidence confidence,
        String rationale,
        List<String> requiredDocuments,
        CaseStatus status) {}
