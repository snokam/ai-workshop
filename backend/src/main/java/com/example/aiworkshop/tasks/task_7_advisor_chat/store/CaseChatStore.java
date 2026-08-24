package com.example.aiworkshop.tasks.task_7_advisor_chat.store;

import com.example.aiworkshop.tasks.task_7_advisor_chat.model.ChatTurn;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

@Component
public class CaseChatStore {
    private final Map<String, List<ChatTurn>> conversations = new ConcurrentHashMap<>();

    public void append(String caseId, ChatTurn turn) {
        conversations.computeIfAbsent(caseId, id -> new CopyOnWriteArrayList<>()).add(turn);
    }

    public List<ChatTurn> findByCaseId(String caseId) {
        return List.copyOf(conversations.getOrDefault(caseId, List.of()));
    }

    public void deleteAll() {
        conversations.clear();
    }
}
