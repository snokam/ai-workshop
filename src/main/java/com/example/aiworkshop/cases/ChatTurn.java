package com.example.aiworkshop.cases;

import java.util.List;

/**
 * One exchange in a Case Chat: what was asked, what came back, and what it took.
 *
 * <p>Proposals are referred to by identifier rather than carried. A Proposal outlives the turn that
 * raised it and changes state when a Case Handler clicks, so a copy held here would be a second
 * version of it going stale in the transcript.
 *
 * @param proposalIds the Proposals raised during this turn, resolved against the Case's live
 *     Proposals when the conversation is rendered
 */
public record ChatTurn(String question, String answer, List<ToolCall> toolCalls, List<String> proposalIds) {}
