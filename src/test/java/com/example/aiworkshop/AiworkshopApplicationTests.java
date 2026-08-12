package com.example.aiworkshop;

import dev.langchain4j.model.chat.ChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Spring Boot's test context loader calls {@code CommandLineRunner}s, so {@link HelloWorldRunner}
 * would fire a real, billed Gemini request on every build. Switch it off, and override {@link
 * ChatModel} with a mock so the context test needs no GCP credentials at all.
 */
@SpringBootTest(properties = "aiworkshop.hello-world.enabled=false")
class AiworkshopApplicationTests {

    @MockitoBean
    ChatModel chatModel;

    @Test
    void contextLoads() {}
}
