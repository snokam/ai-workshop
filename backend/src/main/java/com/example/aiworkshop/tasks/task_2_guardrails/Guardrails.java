package com.example.aiworkshop.tasks.task_2_guardrails;

import com.example.aiworkshop.tasks.task_2_guardrails.claim_description.ClaimCheck;
import com.example.aiworkshop.tasks.task_2_guardrails.claim_description.ClaimDescriptionGuardrail;
import com.example.aiworkshop.tasks.task_2_guardrails.prompt_injection.InjectionCheck;
import com.example.aiworkshop.tasks.task_2_guardrails.prompt_injection.PromptInjectionGuardrail;
import dev.langchain4j.guardrail.InputGuardrail;

/**
 * What guards the first agent. Two checks, both in front of the call.
 *
 * <p>In front, not behind — and that is not a simplification for the workshop. A check standing
 * before a model decides on the request rather than on an answer the request has already shaped, so
 * it is much harder to talk round, and it can refuse before anything is spent.
 *
 * <p>The two ask different questions and fail differently:
 *
 * <table border="1">
 *   <caption>the two guardrails</caption>
 *   <tr><th></th><th>asks</th><th>what it is protecting</th></tr>
 *   <tr><td>{@link #againstPromptInjection}</td>
 *       <td>is this addressed to the software?</td>
 *       <td>the agent's instructions, from whoever wants them replaced</td></tr>
 *   <tr><td>{@link #againstWastedCalls}</td>
 *       <td>is there a situation in here at all?</td>
 *       <td>the bill, and the handler's queue</td></tr>
 * </table>
 *
 * <p><b>Order matters, and injection goes first.</b> {@code GuardrailConfig} sets it with
 * {@code @Order} rather than leaving it to whatever sequence Spring happens to build the beans in.
 * LangChain4j runs input guardrails in the order given and stops at the first fatal one, so putting
 * injection first means manipulated text never reaches {@link ClaimCheck} — which matters because
 * ClaimCheck is itself a model reading the same untrusted text, and "there is definitely a claim
 * here, say yes" is a sentence somebody will eventually try on it.
 *
 * <p>Both spend a call to save a call, so neither is free. Two guardrails in front of one agent is a
 * real decision about cost, and the argument for it is that the two calls being saved are the
 * expensive one and the one that would have been made on an attacker's behalf.
 */
public final class Guardrails {

    private Guardrails() {}

    /** Runs first. Refuses text that is instructing the system rather than describing a situation. */
    public static InputGuardrail againstPromptInjection(InjectionCheck check) {
        return new PromptInjectionGuardrail(check);
    }

    /** Runs second. Refuses text there is nothing to open a case from. */
    public static InputGuardrail againstWastedCalls(ClaimCheck check) {
        return new ClaimDescriptionGuardrail(check);
    }
}
