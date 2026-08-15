# Required Documents are free-text labels matched by an agent

The domain has four Document Types, but they will not be named until the team settles them together.
A Case Status is a checklist over required documents, so waiting for those names would block the
whole case-handler side.

So each Case carries its own list of Required Documents as plain text labels, and the intake agent —
which already reads the uploaded file — is given that list and reports which label the Document
satisfies. Case Status is then derived in Java from the stored matches. Naming the four types later
becomes a data change rather than a model change, and matching free text to free text is work an LLM
does well and Java cannot do at all.

## Considered options

Naming four provisional Document Types now and typing everything against them. Rejected: provisional
names in an enum outlive their provisionality — they reach API paths, UI labels and the glossary
before anyone revisits them, which is the rename this defers precisely to avoid.

## Consequences

- **The match is a model judgement and can be wrong.** Quality Assessment gates readiness; match
  confidence deliberately does not. Self-reported confidence is weakly calibrated, and a Case
  blocking because a model hedged is worse than a visibly wrong match a handler can correct. A
  second pass with a stronger model over low-confidence matches is the obvious next version.
- **No Case Type.** The required list lives on the Case itself. If Cases later differ by kind, the
  list is what varies — not a new noun.
- **A Document matching no label is stored and shown, but ignored by status.** A workshop audience
  uploads whatever is on their desktop, and under any stricter rule every one of those files would
  jam the Case it landed in.
