package com.example.aiworkshop.tasks.task_6_advisor_chat.model;

import com.example.aiworkshop.tasks.task_6_advisor_chat.proposals.ProposalCard;
import java.util.List;

public record ChatAnswer(ChatTurn turn, List<ProposalCard> proposals) {}
