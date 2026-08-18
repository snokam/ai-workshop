/** Two questions the screens ask about a Document that the backend does not answer directly. */

import type { CaseDetail, UploadedDocument } from '../../api'

/** What a document is to its case. The claimant's side has no case context, so it passes none. */
export type Standing = 'counting' | 'superseded' | 'unmatched'

export function standingOf(doc: UploadedDocument, detail: CaseDetail): Standing {
  if (!doc.analysis.matchedRequiredDocument) return 'unmatched'
  return detail.countingDocumentIds.includes(doc.id) ? 'counting' : 'superseded'
}

/* --- shared -------------------------------------------------------------- */

/**
 * Where to fetch a document's own image, or nothing for a file no <img> can render.
 *
 * The bytes are on the server (ADR 0004), so this survives a reload — unlike the object URL it
 * replaced, which lived and died with the tab that did the uploading.
 */
export function previewOf(doc: UploadedDocument): string | undefined {
  return doc.contentType.startsWith('image/') ? `/api/documents/${doc.id}/file` : undefined
}

/** What a document is to its case. The claimant's side has no case context, so it passes none. */
