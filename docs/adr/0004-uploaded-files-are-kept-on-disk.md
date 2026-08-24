# Uploaded files are kept on disk

The uploaded bytes are written to a configured directory at intake, named by Document identifier,
and the directory is emptied on startup. Everything else in this application stays in memory.

This partly reopens a decision recorded the other way. `UploadedDocument` used to say the bytes were
not kept: the browser already holds the file it just sent, so it can render its own preview, and a
second copy server-side bought nothing.

## What prompted it

The Claim Chat's whole premise is that a Claim Handler can ask a question the screen does not answer.
Some of those questions — "the scan is dark, can you make out the amount at the bottom" — are
questions no Extraction can answer, because an Extraction is what an agent made of a file rather
than the file. The intake agent saw the file once, at upload, and then it was gone with the tab.

An Extraction as the only surviving record of a Document is also the weaker half of a pair the
application already draws a line between. The Quality Assessment exists precisely because a blurry
scan and a crisp one produce the same text; keeping only the text throws away the difference the
system was built to notice.

## Considered options

**Return the file from a tool.** LangChain4j supports returning image content from a tool, which
would let the Claim Chat agent look at the file itself with no second agent. Rejected on a fact
rather than a preference: the documented provider list covers Anthropic, Amazon Bedrock and Google
AI Gemini — not Vertex AI Gemini's module, and not the OpenAI-compatible path. This application runs
on both of the ones it does not cover. PDF content is not a supported tool return type on any of
them. The `DocumentReader` agent exists because of this, and is a better shape regardless: an agent
that knows nothing about the Claim cannot be led by it.

**Keep the bytes in memory, in a map, like everything else.** Rejected on the day of the workshop
rather than in principle. A room of people dragging in phone photos at 5–10 MB each, held for the
life of the process, is a heap that only grows.

**Have the browser re-send the file when the chat needs it.** Rejected: it only works for the tab
that uploaded it, and the Claim Handler is on the other screen entirely.

## Consequences

- **The filename the browser sent is never a path.** Files are named by Document identifier, which
  is generated on the server. The filename is untrusted text that is displayed and otherwise not
  used — intake already documents it that way, and this is the first place it would have mattered.
- **The directory is emptied on startup.** The records that point at these files are in memory and
  do not survive a restart. Bytes that did would be unreachable and unattributable, which is worse
  than losing both together.
- **`UploadedDocument` gains no path component.** The path is derivable from the identifier and the
  configured directory. A stored path is a second source of truth that can disagree with where the
  file actually is.
- **The Claimant's preview is unchanged.** It is still rendered from the file the browser already
  holds. Nothing about that path was touched, and there is no endpoint that serves the bytes back.
- **This is not persistence.** The deferred storage decision stays deferred for Claims, Documents,
  Claim Summaries, Proposals and conversations. What changed is that one thing now outlives a
  request, for one reason, with a lifetime deliberately tied to the process.
