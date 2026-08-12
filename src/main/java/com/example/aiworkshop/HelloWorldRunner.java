package com.example.aiworkshop;

import com.example.aiworkshop.ai.Assistant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Hello-world POC: one round trip to Gemini on startup, so you can see the wiring work. */
@Component
@ConditionalOnProperty(name = "aiworkshop.hello-world.enabled", havingValue = "true", matchIfMissing = true)
class HelloWorldRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(HelloWorldRunner.class);

    private final Assistant assistant;

    HelloWorldRunner(Assistant assistant) {
        this.assistant = assistant;
    }

    @Override
    public void run(String... args) {
        log.info("Calling Gemini on Vertex AI...");
        String answer = assistant.chat("Say hello, and in one sentence say what you can help with.");
        log.info("Gemini replied: {}", answer);
    }
}
