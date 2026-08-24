package com.example.aiworkshop.tasks.task_1_first_agent.model;

import java.util.List;

public record CreatedClaim(
        String id,
        String reference,
        String typeLabel,
        MatchConfidence confidence,
        String rationale,
        List<String> requiredDocuments,
        ClaimStatus status) {}
