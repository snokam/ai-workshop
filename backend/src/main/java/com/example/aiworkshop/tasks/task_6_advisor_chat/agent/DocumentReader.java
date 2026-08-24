package com.example.aiworkshop.tasks.task_6_advisor_chat.agent;

import dev.langchain4j.data.message.Content;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import java.util.List;

public interface DocumentReader {
    @SystemMessage(
            """
            You are looking at one file on behalf of a claim handler who cannot make something out on
            it. You know nothing else about the claim, and you do not need to.

            The question is: {{question}}

            Answer only from what is visible on the file. Quote what you can read. If part of it is
            obscured, cut off, or too poor to be sure of, say which part and say what you can see
            rather than guessing at the rest.

            If the file does not show what was asked about at all, say so plainly and stop. That is a
            useful answer — it is what tells a claim handler to stop looking and ask the claimant
            instead. A plausible answer invented from the rest of the file is the one thing that
            would make this worse than not asking.

            Write in English, whatever language the file is in. Anything you quote off the file —
            field names, values, amounts — stays exactly as it appears there, untranslated.
            """)
    String read(@UserMessage List<Content> file, @V("question") String question);
}
