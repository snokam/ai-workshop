# Task 1 — Your first agent

Connect to the model, write an agent that reads a sentence, and open the claim it describes.

The first time the application talks to a model. You learn how an agent is built in LangChain4j:
you write an interface and a system message, and the framework generates the implementation. The
answer comes back as a record, so the rest of the code can branch on it.

## The parts

Do them in this order. Each is one file, and the `TODO` at the top of it has the steps.

| | File | What it is for |
|---|---|---|
| 1 | [`agent/VertexAiConfig.java`](./agent/VertexAiConfig.java) | Build the model |
| 2 | [`agent/ClaimTypeClassifier.java`](./agent/ClaimTypeClassifier.java) | Write the agent |
| 3 | [`ClaimIntake.java`](./ClaimIntake.java) | Turn the answer into a claim |
