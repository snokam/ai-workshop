package com.example.aiworkshop.tasks.task_5_claim_summary.agent;

import com.example.aiworkshop.tasks.task_5_claim_summary.DocumentForSummary;
import com.example.aiworkshop.tasks.task_1_first_agent.model.Claim;
import dev.langchain4j.service.MemoryId;
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
            TODO — task 6, part 1. Write the expensive agent.

            The expensive agent: every document on a claim, read together, in one prompt on every screen load.

              summarise(@MemoryId String claimId, @V("claimType") String claimType,
                        @V("documents") List<DocumentForSummary> documents)

            The prompt has to make it:
              1. describe the documents rather than address anyone — the claimant and the handler both read it
              2. say where they agree and disagree, since that is the thing one document cannot tell you
              3. never state a figure or date that is not in what it was shown
              4. avoid saying whether the claim should be paid — deciding is the handler's job

            It returns a String, so nothing here is enforced by a schema. Everything you want is in the prompt.

            One more thing, and it is the reason this agent has a @MemoryId. You may have summarised this
            claim before. When you have, the handler has already read that summary and does not want it
            again — lead with what is new since, and say plainly if something that arrived contradicts
            what was already there. When you have not, write the whole thing.

            Whatever you were told last time, the documents you are shown now are the truth. Do not repeat
            a figure or a date from an earlier summary that is not in front of you — see part 2 for what
            that drift does to a summary written from its own previous version.
            """)
    @UserMessage(
            """
            Claim type: {{claimType}}

            The documents attached to this claim:

            {{documents}}
            """)
    String summarise(
            @MemoryId String claimId,
            @V("claimType") String claimType,
            @V("documents") List<DocumentForSummary> documents);
}
