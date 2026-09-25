package com.example.aiworkshop.tasks.task_7_dynamic_form_with_streaming;

import com.example.aiworkshop.tasks.task_7_dynamic_form_with_streaming.agent.ClaimFormHelper;
import com.example.aiworkshop.tasks.task_7_dynamic_form_with_streaming.model.ClaimScenario;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class StreamedHelp {

    private static final Logger log = LoggerFactory.getLogger(StreamedHelp.class);

    private static final ObjectMapper JSON = new ObjectMapper();

    /** Long enough for a slow model and a long answer, short enough that a dead one is not forever. */
    private static final long TIMEOUT_MS = 60_000;

    private final ClaimFormHelper helper;

    StreamedHelp(ClaimFormHelper helper) {
        this.helper = helper;
    }

    /**
     * Starts the speaker and returns the response the browser is already reading.
     *
     * <p>Called at the same moment as the decision, not after it, so the method has to return
     * before a single token exists. That is what {@link SseEmitter} is for.
     *
     * @param soFar whatever is in the box at the moment somebody stopped typing
     */
    public SseEmitter on(String soFar) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        TokenStream tokens = helper.helpWith(ClaimScenario.catalog(), soFar);

        // All three callbacks, then start. Each missing one fails differently and none of them
        // throws: no onCompleteResponse and the answer arrives but the connection never closes; no
        // onError and a model that died in a second keeps the browser waiting the full minute; no
        // start() and nothing happens at all, which looks exactly like a slow model.
        tokens.onPartialResponse(token -> send(emitter, token))
                .onCompleteResponse(response -> emitter.complete())
                .onError(failed -> {
                    log.warn("The help stream failed", failed);
                    emitter.completeWithError(failed);
                })
                .start();

        // Returned before a single token exists. Waiting here would hold the response closed until
        // the answer was finished, which is the one thing streaming is for.
        return emitter;
    }

    /**
     * SseEmitter.send throws a checked exception, and a callback cannot, so it is caught here.
     *
     * <p>Quoted as JSON because the wire format is line-based and tokens are not: SSE strips one
     * space after the colon, and a newline inside a token ends the event early and drops the rest.
     * Inside a string both survive.
     */
    static void send(SseEmitter emitter, String token) {
        try {
            emitter.send(JSON.writeValueAsString(token));
        } catch (Exception e) {
            log.debug("The screen stopped listening mid-answer: {}", e.getMessage());
            emitter.completeWithError(e);
        }
    }
}
