package com.example.aiworkshop.cases;

import java.util.List;

public record ChatAnswer(ChatTurn turn, List<ProposalCard> proposals) {}
