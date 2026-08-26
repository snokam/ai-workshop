# Task 1 — Your first agent

Someone types what happened to them into a text field. The text goes to our backend, which sends it
to an LLM together with a prompt and the five kinds of insurance we cover: travel, home contents,
disability, health treatment and motor. The model picks one, and the backend opens a claim of that
kind, listing the documents that kind needs. If nothing fits, it says so and no claim is opened.

That is an agent in one screen: a prompt, the user's text, and an answer the program can act on. You
learn how one is built in LangChain4j — you write an interface and a system message, and the
framework generates the implementation — and why the answer comes back as a record rather than as
prose.

## The parts

Do them in this order. Each is one file, and the `TODO` at the top of it has the steps.

| | File | What it is for |
|---|---|---|
| 1 | [`agent/VertexAiConfig.java`](./agent/VertexAiConfig.java) | Build the model |
| 2 | [`agent/ClaimTypeClassifier.java`](./agent/ClaimTypeClassifier.java) | Write the agent |
| 3 | [`ClaimIntake.java`](./ClaimIntake.java) | Turn the answer into a claim |
