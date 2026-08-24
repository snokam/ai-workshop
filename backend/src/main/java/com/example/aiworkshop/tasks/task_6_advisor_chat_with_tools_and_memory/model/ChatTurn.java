package com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools_and_memory.model;

import java.util.List;

public record ChatTurn(String question, String answer, List<ToolCall> toolCalls, List<String> proposalIds) {}
