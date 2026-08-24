package com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools_and_memory.store;

import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools_and_memory.model.ChatTurn;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

@Component
public class ClaimChatStore {
    private final Map<String, List<ChatTurn>> conversations = new ConcurrentHashMap<>();

    public void append(String claimId, ChatTurn turn) {
        conversations.computeIfAbsent(claimId, id -> new CopyOnWriteArrayList<>()).add(turn);
    }

    public List<ChatTurn> findByClaimId(String claimId) {
        return List.copyOf(conversations.getOrDefault(claimId, List.of()));
    }

    public void deleteAll() {
        conversations.clear();
    }
}
