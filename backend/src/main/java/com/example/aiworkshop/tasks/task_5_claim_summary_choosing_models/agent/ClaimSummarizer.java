package com.example.aiworkshop.tasks.task_5_claim_summary_choosing_models.agent;

import com.example.aiworkshop.tasks.task_5_claim_summary_choosing_models.DocumentForSummary;
import com.example.aiworkshop.tasks.task_1_first_agent.model.Claim;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import java.util.List;

/**
 * Writes the Claim Summary: what is in a Claim's Documents, taken across all of them.
 *
 * <p>Split from {@link ClaimStatusWriter} by input rather than tidiness. This one needs what the
 * Documents say and only changes when a Document is added; the status prose needs a handful of enum
 * values and is rewritten every time a Claim is opened. One agent doing both would drag this payload
 * through every page view.
 *
 * <p>It is handed {@link DocumentForSummary} rather than the Documents themselves. What an agent is
 * given is a decision, and passing the domain record made it an accident of that record's shape —
 * see the note there.
 */
public interface ClaimSummarizer {


    @SystemMessage(
            """
            You are writing for a claim handler who is about to decide a claim, and who would otherwise
            open every document in it one at a time.

            You are told what kind of claim this is. Read the documents as that kind of claim: what
            matters in a travel claim is not what matters in a disability claim, so let the claim type
            frame what is worth pointing out and what a document of this kind would be expected to
            show.

            Say what has arrived and what it says, across all the documents together. Draw the
            connections between them — the same date, the same amount, the same name, or a
            disagreement between two of them. A disagreement is the single most useful thing you can
            point out; say so plainly when you find one.

            This is not a list of the documents. The claim handler can already see the list, and each
            document already has its own summary. Do not repeat either.

            A few short paragraphs at most. Do not recommend a decision, and do not say what should
            happen next — that is not your job here.

            Write in English, whatever language the documents themselves are in. Field names are
            quoted from the documents and are often not English; do not follow them.
            """)
    @UserMessage(
            """
            Claim type: {{claimType}}

            The documents attached to this claim:

            {{documents}}
            """)
    Result<String> summarise(
            @V("claimType") String claimType,
            @V("documents") List<DocumentForSummary> documents);
}
