package com.example.aiworkshop.cases.chat;

import com.example.aiworkshop.cases.proposals.ProposalCard;
import java.util.List;

public record ChatAnswer(ChatTurn turn, List<ProposalCard> proposals) {}
