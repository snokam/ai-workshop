package com.example.aiworkshop.tasks.task_7_streaming_file_claim_chat;

import com.example.aiworkshop.tasks.task_7_streaming_file_claim_chat.agent.ClaimIntakeSpeaker;
import com.example.aiworkshop.workshop.TaskNotImplementedException;
import com.example.aiworkshop.workshop.WorkshopTask;
import dev.langchain4j.service.TokenStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Carries what the speaker writes to the browser, a token at a time.
 *
 * <p>This is the join between two things that do not know about each other: LangChain4j's {@link
 * TokenStream}, which calls you back as tokens arrive, and Spring's {@link SseEmitter}, which holds
 * the HTTP response open and lets you write into it until you say you are done.
 *
 * <p>Both are push, which is why there is no loop here and nothing to poll. You hand the stream three
 * things to do — on a token, on the end, on a failure — and then start it. The method returns
 * immediately; the response is still being written long after.
 */
@Service
public class InterviewNarration {

    private static final Logger log = LoggerFactory.getLogger(InterviewNarration.class);

    /** Long enough for a slow model and a long answer, short enough that a dead one is not forever. */
    private static final long TIMEOUT_MS = 60_000;

    private final ClaimIntakeSpeaker speaker;

    InterviewNarration(ClaimIntakeSpeaker speaker) {
        this.speaker = speaker;
    }

    /**
     * Starts the speaker and returns the response the browser is already reading.
     *
     * @param transcript the conversation so far
     * @param decision what the interviewer settled on, in words the speaker can put to the claimant
     */
    public SseEmitter narrate(String transcript, String decision) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        TokenStream tokens = speaker.say(transcript, decision);

        // TODO — task 7. Carry the tokens to the browser.
        //
        // TokenStream is push, not pull: nothing arrives until you say what to do with it, and nothing
        // starts until you say to start. Four calls, and it reads as one chain:
        //
        //   tokens.onPartialResponse(token -> send(emitter, token))
        //         .onCompleteResponse(response -> emitter.complete())
        //         .onError(failed -> { log.warn("The interview's narration failed", failed);
        //                              emitter.completeWithError(failed); })
        //         .start();
        //
        // send(...) is written for you below, because SseEmitter.send throws a checked IOException and
        // a lambda cannot.
        //
        // Three ways this goes wrong, and each fails differently:
        //
        //   no .start()            you registered callbacks for a stream nobody asked to run. The
        //                          method returns, the browser holds an open connection, and nothing
        //                          ever arrives — it looks exactly like a slow model.
        //   no .onError(...)       the model fails and nobody completes the emitter. The browser waits
        //                          the full timeout for a request that was over in a second.
        //   no .onCompleteResponse the tokens all arrive and the connection stays open anyway, so the
        //                          screen never knows the answer finished.
        //
        // Return the emitter. Do not wait for the stream — returning is what lets the response start,
        // and blocking here would undo the whole point.

        throw new TaskNotImplementedException(WorkshopTask.STREAMING_FILE_CLAIM_CHAT);
    }

    /** SseEmitter.send throws a checked exception, and a callback cannot, so it is caught here. */
    static void send(SseEmitter emitter, String token) {
        try {
            emitter.send(token);
        } catch (Exception e) {
            log.debug("The screen stopped listening mid-answer: {}", e.getMessage());
            emitter.completeWithError(e);
        }
    }
}
