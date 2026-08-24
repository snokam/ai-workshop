package com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools.model;

import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools.proposals.ProposalCard;
import java.util.List;

public record ChatAnswer(ChatTurn turn, List<ProposalCard> proposals) {}
