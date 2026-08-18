package com.example.aiworkshop;

import dev.langchain4j.model.chat.ChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Nothing here calls a model, but the context still wires every agent to a {@link ChatModel}, and the
 * active provider builds one from credentials a test JVM does not have. Overriding the bean with a
 * mock is what lets this run on any machine without a key.
 */
@SpringBootTest
class AiworkshopApplicationTests {

    @MockitoBean
    ChatModel chatModel;

    @Test
    void contextLoads() {}
}
