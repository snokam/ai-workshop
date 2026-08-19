package com.example.aiworkshop.tasks.task_6_chat.model;

import com.example.aiworkshop.tasks.task_1_first_agent.model.CaseOverview;
import com.example.aiworkshop.tasks.task_4_postprocessing.model.FraudScreening;
import com.example.aiworkshop.tasks.task_2_document_agent.model.UploadedDocument;
import com.example.aiworkshop.tasks.task_6_chat.proposals.DocumentRequest;
import com.example.aiworkshop.tasks.task_6_chat.proposals.ProposalCard;
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
