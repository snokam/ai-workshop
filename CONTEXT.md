# Document Handling

Case handling with AI agents in the loop. Someone uploads documents to a case; agents read them,
say what they are, and judge whether the file is good enough to work with. Insurance is the obvious
setting, but nothing here is specific to it.

## Language

**Document**:
One file uploaded to a case. The file plus everything the agents have worked out about it.
_Avoid_: file, attachment, upload

**Document Type**:
The kind of document a Document is. Four types exist in the domain; they have not been named yet.
_Avoid_: category (used only as the name of the free-text field standing in until the four are fixed)

**Classification**:
The agent's decision about which Document Type a Document is.
_Avoid_: categorisation, tagging

**Extraction**:
The facts lifted out of a Document — the values a human would otherwise read off it and type in.
_Avoid_: metadata, parsing, OCR

**Quality Assessment**:
The agent's verdict on a Document as an artefact: legible, complete, and the document it claims to
be. Says nothing about whether the contents are correct or acceptable. It is advice shown to whoever
uploaded the file, never a gate — an upload is always accepted.
_Avoid_: validation, verification, approval

**Case**:
The unit of work a Document belongs to, and the thing that has a status.
_Avoid_: claim, application, file

## Not settled yet

Terms the domain clearly has, which are deliberately still open:

- **The four Document Types** — not named. The intake agent returns free text until they are.
- **The internal role** — "advisor" and "case handler" have both been used for the person who reads
  across a Case. One word needs to win before any code names it.
- **Case Status** — the notes call for knowing "the status of a case". Whether that is derived from
  which Documents are present or written by an agent is undecided, so it is undefined here.
