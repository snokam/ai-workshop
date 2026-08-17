# Agents write in English, and are handed only what they need

Every agent writes its prose in English, whatever language the Documents are in. Each agent says so
in its own system message rather than inheriting it from anywhere — `AiServices.create` builds an
agent from its interface alone, so the interface has to be the whole of the definition.

The exception is Extraction. An `ExtractedField` name and value are quoted off the Document and stay
exactly as they appear on it: they are evidence a Case Handler matches against the artefact, and a
translated label is indistinguishable from a mistranslated one.

## What prompted it

Three agents, three different language policies, none of them chosen. `DocumentAnalyzer` and
`CaseSummarizer` were both told to write "in the language the document is written in";
`CaseStatusWriter` was told nothing and wrote English by default.

The two were not equally able to obey. `DocumentAnalyzer` reads the file. `CaseSummarizer` never
does — it was handed `List<UploadedDocument>`, and LangChain4j renders a template variable by calling
`toString()` on it, so the prompt was a record dump. Its only evidence of language was a handful of
quoted field names and a merchant name. Given a Norwegian supermarket receipt it inferred
"Scandinavian" and wrote the Case Summary in Swedish, beside an English analysis of the same file.

The instruction asked for a fact one of the agents had no access to. Fixing the phrasing would not
have fixed that.

## Considered options

**Detect the language once and carry it.** `DocumentAnalyzer` gains a `language` component — it is
the only agent that can actually see the file — and it is passed to `CaseSummarizer` as a template
variable. Rejected: it keeps the intent at the cost of a schema field, a plumbing change, and no
defined answer for a Case holding Documents in two languages.

**Language follows the reader.** Claimant-facing text in the Document's language, handler-facing text
in the organisation's. Rejected here for a reason that is about this repo and not about the domain:
the workshop is delivered in English to an English-speaking audience, and `DocumentAnalysis.summary`
is read by both audiences, which forces a third rule to resolve. Worth revisiting if this becomes a
product.

## Consequences

- **A Claimant reads intake advice about their own Norwegian receipt in English.** Accepted
  deliberately; see above.
- **The instruction has to outrank its own context.** Quoted field names stay Norwegian, so the
  prompt says "write in English, whatever language the documents themselves are in" rather than
  naming a language and hoping. A rule that can be re-derived from the input will be.
- **`CaseSummarizer` is handed `DocumentForSummary`, not `UploadedDocument`.** What an agent is given
  is a decision; passing the domain record made it an accident of that record's shape, and adding a
  component to `UploadedDocument` silently edited a prompt. The projection carries the filename,
  category, summary, fields and the bare quality verdict — the verdict tells the agent how far to
  trust a Document, without handing over the Quality Assessment's own prose for it to paraphrase back
  into the Case Summary.
- **None of this is covered by a test.** Asserting that a system message contains the word "English"
  only restates the code. What is pinned instead is the text the model receives, at
  `DocumentForSummaryTest` — including that the ids, MIME type and byte count are absent from it.
- **A cached Case Summary outlives a prompt change.** `CaseSummaryStore` is keyed on the set of
  Documents a summary was written over, which is right for uploads and blind to edits like this one.
  Restart after changing a prompt, or the old paragraphs come straight back.
