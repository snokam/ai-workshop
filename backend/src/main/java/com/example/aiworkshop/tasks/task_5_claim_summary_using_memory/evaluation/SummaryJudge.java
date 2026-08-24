package com.example.aiworkshop.tasks.task_5_claim_summary_using_memory.evaluation;

import dev.langchain4j.model.output.structured.Description;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * A model asked whether another model's answer holds up.
 *
 * <p>Read this one sceptically, because it is the technique in this workshop most likely to be
 * used badly. It is genuinely the only way to score prose at any volume, and it is also a machine
 * for producing agreeable numbers: ask a model whether some text is good and it will usually say
 * yes, and you will have measured its agreeableness rather than the summary.
 *
 * <p>Three things make the difference between a judge and a rubber stamp, and all three are here.
 * The question is one specific thing that could be plainly false, not "is this good". The judge is
 * told to answer no when unsure, because the failure that costs is a bad summary marked fine. And
 * it must quote the words that made it answer — a judge that cannot point at anything is guessing,
 * and you can see that in the output instead of trusting the score.
 *
 * <p>What none of that fixes: this is the same model marking its own homework, and it shares every
 * blind spot with the agent that wrote the summary. Whatever both of them are wrong about, this
 * will not find. That is not a reason to skip it — it is a reason not to report the number without
 * saying what produced it.
 */
public interface SummaryJudge {

    @SystemMessage(
            """
            You are checking one specific thing about a piece of text, on behalf of someone who has to
            decide whether it can be shown to an insurance claim handler.

            You are given the question, the documents the text was written from, and the text itself.
            Answer only the question you were asked. Do not comment on anything else about the text,
            however wrong it seems — there are other questions, and this is not the one.

            Answer no if you are not sure. A summary wrongly marked acceptable reaches a claim handler
            who has no reason to doubt it. A summary wrongly marked unacceptable costs someone two
            minutes of reading.

            Quote the words that decided it, from the text itself. If nothing in the text settles the
            question either way, say so in the quote field and answer no.
            """)
    @UserMessage(
            """
            The question: {{question}}

            The documents it was written from:
            {{documents}}

            The text:
            {{summary}}
            """)
    Verdict judge(@V("question") String question, @V("documents") String documents, @V("summary") String summary);

    record Verdict(
            @Description("true only if the answer to the question is clearly yes") boolean holds,
            @Description("The words from the text that decided it, quoted. Say so plainly if nothing did.")
                    String quote,
            @Description("One sentence of reasoning.") String because) {}
}
