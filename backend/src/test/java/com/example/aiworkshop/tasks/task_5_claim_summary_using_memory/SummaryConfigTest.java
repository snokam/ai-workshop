package com.example.aiworkshop.tasks.task_5_claim_summary_using_memory;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.aiworkshop.tasks.task_5_claim_summary_using_memory.agent.SummaryConfig;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import org.junit.jupiter.api.Test;

/**
 * One memory per claim, and the reason it has to be checked.
 *
 * <p>A provider that ignores its id and hands back the same memory to everyone compiles, starts, and
 * behaves perfectly on one claim. The failure needs two: open a second claim and the summariser is
 * looking at the first one's documents, so one claimant's receipts get described on another
 * claimant's screen. Nothing throws, and the summary reads as fluently as ever.
 *
 * <p>No model here. Whether each claim gets its own memory is decided by ordinary code, which is why
 * this is the part of task 5 that can be asserted at all.
 */
class SummaryConfigTest {

    @Test
    void eachClaimGetsItsOwnMemory() {
        ChatMemoryProvider provider = SummaryConfig.summaryMemory();

        ChatMemory one = provider.get("claim-1");
        ChatMemory another = provider.get("claim-2");

        assertThat(one)
                .describedAs("two claims sharing one memory means one claimant's documents are"
                        + " summarised onto another claimant's screen — build the memory inside the"
                        + " lambda, not once outside it")
                .isNotSameAs(another);
    }

    @Test
    void theMemoryIsKeyedByTheIdItWasGiven() {
        ChatMemoryProvider provider = SummaryConfig.summaryMemory();

        assertThat(provider.get("claim-1").id()).isEqualTo("claim-1");
    }
}
