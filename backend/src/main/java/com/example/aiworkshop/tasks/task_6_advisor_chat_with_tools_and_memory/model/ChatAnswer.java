package com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools_and_memory.model;

import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools_and_memory.proposals.ProposalCard;
import java.util.List;

public record ChatAnswer(ChatTurn turn, List<ProposalCard> proposals) {}
