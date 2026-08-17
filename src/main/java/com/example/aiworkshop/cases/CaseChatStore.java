package com.example.aiworkshop.cases;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

/**
 * The Case Chat as a Case Handler sees it: the turns, in order, per Case.
 *
 * <p>Separate from the agent's own chat memory, and deliberately so. That memory is a window of the
 * last twenty messages, holds tool calls and tool results the handler has no use for, and is shaped
 * for a model. This is shaped for a screen.
 *
 * <p>There is no authentication, so a conversation belongs to the Case rather than to a person. Two
 * Case Handlers with the same Case open share one. That is the intended model, not a limitation.
 *
 * <p>In memory, so everything is lost on restart, like every other store here.
 */
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
