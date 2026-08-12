package com.example.aiworkshop;

import com.example.aiworkshop.ai.Assistant;
import dev.langchain4j.model.chat.ChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Hello-world POC: one round trip on startup, so you can see the active provider work. */
@Component
@ConditionalOnProperty(name = "aiworkshop.hello-world.enabled", havingValue = "true", matchIfMissing = true)
class HelloWorldRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(HelloWorldRunner.class);

    private final Assistant assistant;
    private final ChatModel chatModel;

    HelloWorldRunner(Assistant assistant, ChatModel chatModel) {
        this.assistant = assistant;
        this.chatModel = chatModel;
    }

    @Override
    public void run(String... args) {
        log.info("Calling model provider: {}", chatModel.provider());
        String answer = assistant.chat("Say hello, and in one sentence say what you can help with.");
        log.info("Model replied: {}", answer);
    }
}
