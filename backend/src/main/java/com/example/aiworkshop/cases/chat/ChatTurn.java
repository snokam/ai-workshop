package com.example.aiworkshop.cases.chat;

import java.util.List;

public record ChatTurn(String question, String answer, List<ToolCall> toolCalls, List<String> proposalIds) {}
