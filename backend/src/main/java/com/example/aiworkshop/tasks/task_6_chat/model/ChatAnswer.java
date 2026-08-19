package com.example.aiworkshop.tasks.task_6_chat.model;

import com.example.aiworkshop.tasks.task_6_chat.proposals.ProposalCard;
import java.util.List;

public record ChatAnswer(ChatTurn turn, List<ProposalCard> proposals) {}
