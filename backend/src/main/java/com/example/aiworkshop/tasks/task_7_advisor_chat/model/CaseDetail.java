package com.example.aiworkshop.tasks.task_7_advisor_chat.model;

import com.example.aiworkshop.tasks.task_1_first_agent.model.CaseOverview;
import com.example.aiworkshop.tasks.task_5_fraud_detection.model.FraudScreening;
import com.example.aiworkshop.tasks.task_3_document_agent.model.UploadedDocument;
import com.example.aiworkshop.tasks.task_7_advisor_chat.proposals.DocumentRequest;
import com.example.aiworkshop.tasks.task_7_advisor_chat.proposals.ProposalCard;
import java.util.List;

public record CaseDetail(
        CaseOverview overview,
        List<UploadedDocument> documents,
        List<String> countingDocumentIds,
        List<String> blockedDocumentIds,
        String summary,
        String statusNote,
        List<FraudScreening> screenings,
        List<DocumentRequest> documentRequests,
        List<ProposalCard> proposals,
        List<ChatTurn> conversation) {}
