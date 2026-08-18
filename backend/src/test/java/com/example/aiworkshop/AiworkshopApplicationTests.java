package com.example.aiworkshop;

import dev.langchain4j.model.chat.ChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class AiworkshopApplicationTests {
    @MockitoBean
    ChatModel chatModel;

    @Test
    void contextLoads() {}
}
