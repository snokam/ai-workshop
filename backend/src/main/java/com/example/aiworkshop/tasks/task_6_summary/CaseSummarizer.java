package com.example.aiworkshop.tasks.task_6_summary;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import java.util.List;
import com.example.aiworkshop.cases.DocumentForSummary;

/**
 * Writes the Case Summary: what is in a Case's Documents, taken across all of them.
 *
 * <p>Split from {@link CaseStatusWriter} by input rather than tidiness. This one needs what the
 * Documents say and only changes when a Document is added; the status prose needs a handful of enum
 * values and is rewritten every time a Case is opened. One agent doing both would drag this payload
 * through every page view.
 *
 * <p>It is handed {@link DocumentForSummary} rather than the Documents themselves. What an agent is
 * given is a decision, and passing the domain record made it an accident of that record's shape —
 * see the note there.
 */
public interface CaseSummarizer {
    /**
     * ── TASK SUMMARY ────────────────────────────────────────────────────────────────────────
     * Set to true once you have written the case summariser below. While it is false the
     * application still runs: every screen that does not need this agent works as normal,
     * and the one that does explains which file to open.
     * ──────────────────────────────────────────────────────────────────────────────────
     */
    boolean IMPLEMENTED = false;


    @SystemMessage(
            """
            TODO — task 6.

            Write the system message for the agent that reads every document on a case at once and
            says what they add up to: what is established, what disagrees, and what is still
            missing.

            It is shown DocumentForSummary, not the files — read that record to see what it does and
            does not get.

            The solutions branch has the version this was written from.
            """)
    @UserMessage(
            """
            Case type: {{caseType}}

            The documents attached to this case:

            {{documents}}
            """)
    String summarise(@V("caseType") String caseType, @V("documents") List<DocumentForSummary> documents);
}
