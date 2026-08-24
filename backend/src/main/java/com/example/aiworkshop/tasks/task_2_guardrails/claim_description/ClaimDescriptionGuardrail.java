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
 */
public class ClaimDescriptionGuardrail implements InputGuardrail {

    private final ClaimCheck check;

    public ClaimDescriptionGuardrail(ClaimCheck check) {
        this.check = check;
    }

    @Override
    public InputGuardrailResult validate(UserMessage message) {
        // TODO — task 2, part 2. Ask it, and refuse.
        //
        // Steps:
        //
        //   1. message.singleText() is what the person typed
        //   2. ClaimCheck.Verdict verdict = check.couldOpenAClaimFrom(...)
        //   3. verdict.couldOpenAClaim() ? success() : fatal(verdict.whatWouldHelp())
        //
        // fatal(...) and success() come from InputGuardrail, which this class implements.
        //
        // Resist adding a length rule or a list of greetings underneath. A rule about length refuses
        // "Bilen ble stjalet" and lets "asdf asdf asdf asdf" through — both mistakes at once, and no
        // tuning fixes it, because whether there is a situation in a piece of text is a question about
        // meaning.

        return success();
    }

    @Override
    public InputGuardrailResult validate(InputGuardrailRequest request) {
        return validate(request.userMessage());
    }
}
