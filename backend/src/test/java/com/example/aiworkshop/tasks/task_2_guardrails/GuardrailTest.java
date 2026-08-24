package com.example.aiworkshop.tasks.task_2_guardrails;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.aiworkshop.tasks.task_2_guardrails.guardrails.Guardrails;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrailResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * The guardrail in front of the first agent.
 *
 * <p>Everything here is decided on the text alone, so none of it needs a model, credentials or a
 * network. That is not a convenience of the test — it is the property that makes an input guardrail
 * worth having: it costs nothing when it refuses.
 */
class GuardrailTest {

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "bil", "hjelp", "?????"})
    void thereHasToBeEnoughToGoOn(String nothingMuch) {
        assertThat(refused(nothingMuch)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"hei", "Hei!", "hello", "Good morning", "test"})
    void aGreetingIsHowAMessageStartsNotWhatItSays(String greeting) {
        assertThat(refused(greeting)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "Someone reversed into my parked car outside the shop.",
                "My suitcase never turned up after my flight home.",
                "Det var innbrudd i leiligheten min forrige uke.",
                "I have a question about whether my policy covers a flooded cellar."
            })
    void anythingSomebodyCouldOpenACaseFromGoesThrough(String description) {
        assertThat(refused(description)).isFalse();
    }

    /**
     * The guardrail decides whether there is anything to read, not whether the situation is
     * insurable. Deciding that is the classifier's job, and it has a case type for exactly this.
     */
    @Test
    void itDoesNotTryToDecideWhetherTheClaimIsAnyGood() {
        assertThat(refused("My neighbour keeps parking across my drive and I want to complain."))
                .isFalse();
    }

    private static boolean refused(String description) {
        InputGuardrailResult result = Guardrails.beforeTheCall().validate(UserMessage.from(description));
        return !result.isSuccess();
    }
}
