package com.example.aiworkshop.cases.model;

import com.example.aiworkshop.cases.proposals.DocumentRequest;
import java.util.List;

public record CaseOverview(
        String id,
        String reference,
        String typeLabel,
        CaseStatus status,
        List<String> requiredDocuments,
        List<String> outstanding,
        List<DocumentRequest> documentRequests) {}
