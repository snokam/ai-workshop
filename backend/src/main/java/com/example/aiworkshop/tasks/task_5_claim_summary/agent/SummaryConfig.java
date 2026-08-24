package com.example.aiworkshop.tasks.task_5_claim_summary.agent;

import com.example.aiworkshop.workshop.TaskNotImplementedException;
import com.example.aiworkshop.workshop.UnfinishedTasks;
import com.example.aiworkshop.workshop.WorkshopTask;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Task 5's wiring: the expensive agent, the cheap one beside it, and what the expensive one remembers. */
@Configuration
public class SummaryConfig {

    @Bean
    ClaimSummarizer claimSummarizer(ChatModel chatModel) {
        return UnfinishedTasks.wire(
                ClaimSummarizer.class,
                WorkshopTask.CLAIM_SUMMARY,
                () -> AiServices.builder(ClaimSummarizer.class)
                        .chatModel(chatModel)
                        .chatMemoryProvider(summaryMemory())
                        .build());
    }

    /**
     * What the summariser remembers between one look at a claim and the next.
     *
     * <p>A handler who read this claim yesterday does not want the same six sentences plus two. They
     * want what arrived since, and whether it agrees with what was already there. The agent can only
     * write that if it can see what it said last time, which is what a memory is.
     *
     * <p>This is the first memory in the workshop; task 6's chat has one too, and it is given there.
     *
     * <p>Public so a test and the progress bar can ask about it without a model in the room.
     */
    public static ChatMemoryProvider summaryMemory() {
        // TODO — task 5, part 2. Give the summariser a memory.
        //
        // Return a ChatMemoryProvider. It is handed the @MemoryId — the claim id — and returns the
        // memory for that claim:
        //
        //   return claimId -> MessageWindowChatMemory.withMaxMessages(10);
        //
        // Two things to get right, and only one of them is the number.
        //
        // ONE MEMORY PER CLAIM. Build the memory inside the lambda, not once outside it:
        //
        //   ChatMemory shared = MessageWindowChatMemory.withMaxMessages(10);   // WRONG
        //   return claimId -> shared;
        //
        // That compiles, runs, and looks fine on one claim. Open a second and the summariser has the
        // first claim's documents in front of it — one claimant's receipts described on another
        // claimant's screen. The lambda takes an id precisely so this cannot happen, and it is the only
        // thing SummaryConfigTest checks, because it is the only part that can be checked without a
        // model.
        //
        // THE WINDOW IS A TRADE, and it is the real exercise. A message here is not a chat line: it is
        // a whole claim's documents rendered into a prompt, plus the summary that came back. So:
        //
        //   small window   cheap, and it forgets the documents first. The agent then writes its next
        //                  summary from its own previous summary — a copy of a copy. Watch it over
        //                  three uploads and count what has gone missing by the third.
        //   large window   grounded and dear. Every past round is re-sent on every screen load, so a
        //                  claim with ten documents pays for all ten every time somebody opens it.
        //
        // Pick a number, then upload three documents one at a time and read what it says on each. Then
        // change the number and do it again. The rubric in part 3 is how you tell which was better.

        throw new TaskNotImplementedException(WorkshopTask.CLAIM_SUMMARY);
    }

    @Bean
    ClaimStatusWriter claimStatusWriter(ChatModel chatModel) {
        return AiServices.create(ClaimStatusWriter.class, chatModel);
    }
}
