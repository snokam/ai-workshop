# Task 8 — Case: File claim with AI chat

An extra, and optional. Task 1 read one sentence and committed to a case type on the spot. This one
is allowed to ask first.

**Time:** 30 minutes. **You need:** task 1 read for comparison, and task 3 working if you want to

The parts, in order, are in `backend/src/main/java/com/example/aiworkshop/tasks/task_8_create_case_chat/README.md` — each one names the file, what it is for, and what to reach for.
follow a chat all the way to uploading. Nothing here touches task 1 — it lives beside it.

## The one difference that matters

A one-shot classifier has to guess when the description is thin. "Something happened on my trip" is
travel, but a cancelled trip and a stolen bag need different documents, and the sentence does not say
which. Task 1 picks anyway. This agent may put one question back before it decides.

Everything else follows from that single change of shape:

**It chooses a scenario, not a type.** `CaseScenario` is finer than `CaseType`: `TRAVEL_CANCELLATION`
and `TRAVEL_BAGGAGE` are both `TRAVEL`, but each owns its own checklist. The case is still opened as a
`CaseType` a handler recognises — the scenario is only how the interview decides which documents to
ask for. Read the enum and note that `catalog()` is what the prompt is shown, rendered from the same
values a case is opened from, so the taxonomy the agent reasons over cannot drift from the one it
picks from.

**One call does two things.** `InterviewTurn` is the output schema, and it is a discriminated record:
`NEEDS_INFO` fills `questions` and leaves `scenario` null, `DECIDED` fills `scenario` and leaves
`questions` empty. There is no union in a JSON schema, so `decision` is what says which half to read.
Constraining `scenario` to the enum is what stops the agent inventing a situation with no checklist
behind it.

**It holds no memory.** The screen resends the whole conversation each turn as `{{transcript}}`; the
agent is a pure function of it. The state lives in the browser, not the server — the same reason the
summariser in task 6 is handed a projection rather than fetching one.

## What to write

Open
`backend/src/main/java/com/example/aiworkshop/tasks/task_8_create_case_chat/agent/CaseIntakeInterviewer.java`
and write the `@SystemMessage`. It has to do two jobs well:

- **Ask only what changes the answer.** A question is worth putting to a person only if two scenarios
  need different documents and you cannot yet tell them apart. Anything else is a form, not a
  conversation. Cap it — one to three questions, and lean towards deciding once the transcript shows
  you have already asked.
- **Decide cleanly.** When one scenario fits, return it with honest confidence, and fall back to
  `OTHER` rather than forcing a poor match.

The README beside the file breaks this into parts and says what each one is for. There is no
answer in the file to peek at — the finished version is on the `solutions` branch, for afterwards.

Then set `IMPLEMENTED = true`. That is the only file this task asks you to touch.

The rest of the feature is plumbing, in `backend/src/main/java/com/example/aiworkshop/cases/interview/`
— the controller, the `InterviewTurn` schema, the `CaseScenario` taxonomy, and `InterviewCaseOpener`,
which writes into the same `CaseStore` as task 1 but mints its own references so the two intakes never
collide. Worth a read; nothing to change.

## How you know it worked

```bash
cd backend && ./mvnw test -Dtest=TaskCompletionTest
```

Then open **Report with AI chat** from the front page. Type something deliberately vague — "our
holiday didn't happen" — and the agent should ask what went wrong before opening anything. Answer,
and it should land you on a case whose checklist matches what you said, not the whole of travel.

## Try to break it

- A description that is already precise: "my flight home lost my suitcase". It should *not* ask — it
  has enough. An agent that always asks is as useless as one that never does.
- Answer every question with "I don't know". It has to decide anyway; watch it fall back.
- Say something that is no kind of insurance at all. `OTHER`, not a forced travel case.
- Contradict yourself between the description and an answer. Which does it trust?

## If you finish early

- **Split another type.** `HOME_CONTENTS` is one general scenario today; theft and water damage need
  different papers. Add the scenarios and watch the questions change with no prompt edit.
- **Let it re-open.** Right now a decision is final. What would it take to let the person correct it
  and re-run from the transcript?
- **Give it a budget.** Make "never more than three questions" a rule the code enforces, not a line in
  the prompt, and decide what happens on the fourth turn.
