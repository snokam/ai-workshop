package com.example.aiworkshop.tasks.task_1_first_agent.model;

import java.util.List;

/** A claim on the list: what it is, and how much of it is still missing. */
public record ClaimOverview(
        String id,
        String reference,
        String typeLabel,
        ClaimStatus status,
        List<String> requiredDocuments,
        List<String> outstanding) {}
