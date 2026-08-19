package com.example.aiworkshop.tasks.task_1_first_agent.model;

import java.util.List;

/** A case on the list: what it is, and how much of it is still missing. */
public record CaseOverview(
        String id,
        String reference,
        String typeLabel,
        CaseStatus status,
        List<String> requiredDocuments,
        List<String> outstanding) {}
