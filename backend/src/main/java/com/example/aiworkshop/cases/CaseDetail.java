package com.example.aiworkshop.cases;

import com.example.aiworkshop.document.UploadedDocument;
import com.example.aiworkshop.tasks.task_4_postprocessing.model.FraudScreening;
import java.util.List;

public record CaseDetail(
        CaseOverview overview,
        List<UploadedDocument> documents,
        List<String> countingDocumentIds,
        List<String> blockedDocumentIds,
        String summary,
        String statusNote,
        List<FraudScreening> screenings,
        List<ProposalCard> proposals,
        List<ChatTurn> conversation) {}
