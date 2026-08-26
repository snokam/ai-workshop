package com.example.aiworkshop.tasks.task_7_dynamic_form_with_streaming;

import com.example.aiworkshop.tasks.task_7_dynamic_form_with_streaming.agent.ClaimFormHelper;
import com.example.aiworkshop.tasks.task_7_dynamic_form_with_streaming.model.ClaimScenario;
import dev.langchain4j.service.TokenStream;
import tools.jackson.databind.ObjectMapper;
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

    /** Only ever asked to quote a string, so the default configuration is the whole of it. */
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

        tokens.onPartialResponse(token -> send(emitter, token))
                .onCompleteResponse(response -> emitter.complete())
                .onError(failed -> {
                    log.warn("The interview's narration failed", failed);
                    emitter.completeWithError(failed);
                })
                .start();

        return emitter;
    }

    /**
     * One token, on the wire, in a form that survives the trip.
     *
     * <p>Sent as JSON rather than as itself, and both reasons are things that only show up in the
     * output. Server-sent events are line-based, so a token containing a newline would arrive as two
     * frames and the text would come apart. And the format is {@code data:<value>}, where a reader is
     * required to drop one leading space — so a token that begins with a space, which plenty do,
     * silently loses it and two words run together.
     *
     * <p>Spring's String converter writes a string verbatim whatever media type it is given, so the
     * quoting has to be done here rather than asked for.
     *
     * <p>{@code SseEmitter.send} also throws a checked exception, which a callback cannot, so it is
     * caught here rather than in the lambda.
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
