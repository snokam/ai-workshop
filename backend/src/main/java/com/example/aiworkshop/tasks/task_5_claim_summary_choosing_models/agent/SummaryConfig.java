package com.example.aiworkshop.tasks.task_5_claim_summary_choosing_models.agent;

import com.example.aiworkshop.workshop.TaskNotImplementedException;
import com.example.aiworkshop.workshop.UnfinishedTasks;
import com.example.aiworkshop.workshop.WorkshopTask;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Task 5's wiring: two agents, two jobs of very different difficulty, and the question of whether
 * they need the same model.
 *
 * <p>Everything before this task has had one model, and never asked. Here there are two agents side
 * by side and the difference between their jobs is obvious once it is pointed at:
 *
 * <table border="1">
 *   <caption>the two jobs</caption>
 *   <tr><th></th><th>is given</th><th>has to</th></tr>
 *   <tr><td>{@link ClaimSummarizer}</td>
 *       <td>every document on the claim at once</td>
 *       <td>read across them, notice where they disagree, write prose</td></tr>
 *   <tr><td>{@link ClaimStatusWriter}</td>
 *       <td>facts already worked out — the status, what is outstanding</td>
 *       <td>put them in one sentence</td></tr>
 * </table>
 */
@Configuration
public class SummaryConfig {

    /** Which of the two jobs a model is being picked for. */
    public enum Job {
        /** Read every document on the claim and write what they say together. */
        READING_EVERY_DOCUMENT,
        /** Turn facts that are already worked out into one sentence. */
        WRITING_THE_STATUS_LINE
    }

    /**
     * Which model each job runs on.
     *
     * <p>One method, so the decision is in one place and can be argued with. Both models are built in
     * task 1 and handed in here: {@code best} is what the whole workshop has been using, {@code
     * cheaper} is a smaller one beside it.
     */
    public static ChatModel modelFor(Job job, ChatModel best, ChatModel cheaper) {
        // TODO — task 5. Decide which model each job needs.
        //
        // Return best or cheaper for each Job. Two lines, and the whole of the task is which way round
        // you put them and whether you can say why.
        //
        // Measure before you choose. Both agents return Result<String>, so SummaryDesk already logs
        // what every call cost — open a claim and read the log. Measured on the status line while this
        // was written:
        //
        //   gemini-2.5-flash        2.07s   87 tokens   "The motor claim is awaiting a police report
        //                                                to proceed."
        //   gemini-2.5-flash-lite   0.63s   64 tokens   "**Claim:** The motor claim is pending the
        //                                                arrival of the police report. **Next Move:**
        //                                                Follow up with the claimant..."
        //
        // Three times faster and cheaper, and it ignored "one short sentence" — markdown, two
        // fragments, in a line that is rendered as plain text. That is the trade, and both halves of it
        // are real: the saving is not imaginary and neither is the mess.
        //
        // So it is not "use the cheap one where you can". It is: this job is easy enough that a smaller
        // model can do it, IF you can get the format you need out of it. If you cannot, either tighten
        // that agent's prompt until you can, or pay for the better one and know what you are paying for.
        //
        // One more number, and it is the one that surprises people. On the summariser earlier:
        //
        //   inputTokenCount = 54, outputTokenCount = 51, totalTokenCount = 244
        //
        // 54 and 51 do not make 244. The other 139 are thinking tokens — spent, billed, and invisible
        // in both the prompt and the answer. Any cost estimate built from what you can see is wrong.
        //
        // ./mvnw test -Pevaluate runs the rubric over the summary, which is how you tell whether a
        // model change made the answer worse rather than only cheaper.

        throw new TaskNotImplementedException(WorkshopTask.CLAIM_SUMMARY_CHOOSING_MODELS);
    }

    /**
     * Both agents wait on the one decision, because until it is made there is no model to build them
     * on. An unwritten task stops the feature it provides and nothing else: the claim screen says
     * which file to open, and everything around it keeps working.
     */
    @Bean
    ClaimSummarizer claimSummarizer(ChatModel chatModel, @Qualifier("cheaper") ChatModel cheaperChatModel) {
        if (!decided()) {
            return UnfinishedTasks.notWrittenYet(ClaimSummarizer.class, WorkshopTask.CLAIM_SUMMARY_CHOOSING_MODELS);
        }
        return AiServices.create(
                ClaimSummarizer.class, modelFor(Job.READING_EVERY_DOCUMENT, chatModel, cheaperChatModel));
    }

    @Bean
    ClaimStatusWriter claimStatusWriter(ChatModel chatModel, @Qualifier("cheaper") ChatModel cheaperChatModel) {
        if (!decided()) {
            return UnfinishedTasks.notWrittenYet(ClaimStatusWriter.class, WorkshopTask.CLAIM_SUMMARY_CHOOSING_MODELS);
        }
        return AiServices.create(
                ClaimStatusWriter.class, modelFor(Job.WRITING_THE_STATUS_LINE, chatModel, cheaperChatModel));
    }

    private static boolean decided() {
        return UnfinishedTasks.written(() -> modelFor(Job.READING_EVERY_DOCUMENT, null, null));
    }
}
