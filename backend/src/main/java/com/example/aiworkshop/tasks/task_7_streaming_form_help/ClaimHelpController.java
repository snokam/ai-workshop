package com.example.aiworkshop.tasks.task_7_streaming_form_help;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * One endpoint, and it stays open.
 *
 * <p>Everything else in this application answers with JSON once it has an answer. This holds the
 * response while a model writes into it, which is a different kind of endpoint and needs a different
 * kind of return type — {@link SseEmitter} rather than a record.
 *
 * <p>It is a POST because what the claimant has typed can be long and belongs in a body rather than a
 * query string. The browser reads the body as it arrives instead of awaiting the whole of it.
 */
@RestController
@RequestMapping("/api/claims/help")
class ClaimHelpController {

    private final StreamedHelp help;

    ClaimHelpController(StreamedHelp help) {
        this.help = help;
    }

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    SseEmitter helpWith(@RequestBody HelpRequest request) {
        return help.on(request.soFar());
    }

    /** Whatever is in the box at the moment somebody stopped typing. */
    record HelpRequest(String soFar) {}
}
