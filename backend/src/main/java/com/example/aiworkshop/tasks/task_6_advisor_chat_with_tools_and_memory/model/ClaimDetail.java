package com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools_and_memory.model;

import com.example.aiworkshop.tasks.task_1_first_agent.model.ClaimOverview;
import com.example.aiworkshop.tasks.task_3_document_agent.model.UploadedDocument;
import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools_and_memory.proposals.DocumentRequest;
import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools_and_memory.proposals.ProposalCard;
import java.util.List;

public record ClaimDetail(
        ClaimOverview overview,
        List<UploadedDocument> documents,
        List<String> countingDocumentIds,
        List<String> blockedDocumentIds,
        String summary,
        String statusNote,
        List<DocumentRequest> documentRequests,
        List<ProposalCard> proposals,
        List<ChatTurn> conversation) {}
