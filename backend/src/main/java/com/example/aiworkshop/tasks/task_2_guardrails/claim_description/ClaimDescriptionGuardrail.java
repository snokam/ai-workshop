package com.example.aiworkshop.tasks.task_2_guardrails.claim_description;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailRequest;
import dev.langchain4j.guardrail.InputGuardrailResult;

/**
 * Refuses text nobody could open a claim from, before the classifier is asked about it.
 *
 * <p>The judgement belongs to {@link ClaimCheck}, which is a model, and this class does nothing but
 * ask it and pass on the answer. There is no length rule and no list of greetings underneath,
 * deliberately: whether there is a situation in a piece of text is a question about meaning. A rule
 * about length refuses "Bilen ble stjålet" and lets "asdf asdf asdf asdf" through, and both of those
 * are the wrong way round.
 *
 * <p>Worth knowing what this costs. A guardrail that counted characters would be free when it
 * refused; this one spends a call to save a call. It is worth it because the call it saves is the
 * more expensive of the two, and because nothing cheaper can answer the question at all — but "put a
 * guardrail in front of it" is not the same as "make it free", and the difference shows up on the
 * bill.
 *
 * <p>With one exception: an empty box has no meaning to read, so there is no judgement to buy — and
 * LangChain4j rejects a blank prompt before it builds the request anyway. Note how narrow that test
 * is. It is emptiness, not shortness; "asdf asdf asdf asdf" is longer than a real claim and still
 * says nothing, and only the model tells those two apart.
 */
public class ClaimDescriptionGuardrail implements InputGuardrail {

    /** In English, because a text with no words in it names no language to answer in. */
    public static final String NOTHING_THERE = "Please tell us what happened, in a sentence or two.";

    private final ClaimCheck check;

    public ClaimDescriptionGuardrail(ClaimCheck check) {
        this.check = check;
    }

    @Override
    public InputGuardrailResult validate(UserMessage message) {
        String description = message.singleText();
        if (description == null || description.isBlank()) {
            return fatal(NOTHING_THERE);
        }

        ClaimCheck.Verdict verdict = check.couldOpenAClaimFrom(description);

        return verdict.couldOpenAClaim() ? success() : fatal(verdict.whatWouldHelp());
    }

    @Override
    public InputGuardrailResult validate(InputGuardrailRequest request) {
        return validate(request.userMessage());
    }
}
