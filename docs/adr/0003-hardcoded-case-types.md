# Case Types are a hardcoded enum with per-type Required Documents

The claimant side now opens with a free-text box: someone writes what they need help with, an agent
reads it, and a Case is opened for them. For that Case to have a checklist, something has to turn
"my suitcase never arrived" into a set of Required Documents.

So a small, fixed set of **Case Types** lives in an enum (`CaseType`), each carrying a display label,
a one-line description the classifier reads, and the Required Documents a Case of that kind is created
with. A new agent (`CaseTypeClassifier`) is handed the rendered catalogue and the claimant's words,
and returns one of those types — or `OTHER` when none fit. Picking the type and writing the checklist
are then the same act: the enum constant is both.

This is a deliberate reversal of [ADR 0001](./0001-free-text-required-documents.md)'s "No Case Type".
The chosen type is kept on the Case, and the handler-side agents are handed it so they read a travel
claim as a travel claim. The types are grounded in real Storebrand products (travel, home contents,
disability, health treatment, motor) so the workshop has something recognisable to describe against.

## Considered options

Having the agent invent the Required Documents from the description, with no fixed types. Rejected:
the checklist would vary run to run for the same kind of case, and there would be nothing stable to
show a claimant or to seed a handler demo from — the whole point of a checklist is that it is the
same list every time for the same kind of case.

## Consequences

- **The type is kept on the Case and frames the handler agents.** Both the Case Summary and the
  status note are handed the type's label, so what is worth pointing out in a travel claim is not
  what is pointed out in a disability one. It does not touch `CaseStatus`, which is still derived
  from the Required Documents alone.
- **`OTHER` opens a Case with no checklist.** An off-topic or unplaceable description is still
  accepted rather than refused — the same stance the document side takes on a file that matches
  nothing. With nothing required, such a Case is `READY_FOR_DECISION` from the start.
- **Adding a Case Type is a data change.** A new enum constant updates both the classifier's catalogue
  and the checklist it creates, because `CaseType.catalog()` is rendered from the same values.
