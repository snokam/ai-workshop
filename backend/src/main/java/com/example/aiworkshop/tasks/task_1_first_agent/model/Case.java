package com.example.aiworkshop.tasks.task_1_first_agent.model;

import java.util.List;

/**
 * A case, as task 1 knows it: what was reported, and what has to arrive before anyone can decide.
 *
 * <p>There is nothing here about the documents themselves, and that is deliberate. Task 1 can say
 * which documents a motor claim needs; it cannot say whether the photo that arrived is one of them,
 * because reading the photo is task 2's job. That question lives in task 2, on CaseDocuments, and
 * this record stays answerable by a participant who has only written the first agent.
 */
public record Case(String id, String reference, CaseType type, List<String> requiredDocuments) {}
