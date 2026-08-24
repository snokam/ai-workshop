
import type { CaseDetail, UploadedDocument } from '../../api'

export type Standing = 'counting' | 'superseded' | 'unmatched'

export function standingOf(doc: UploadedDocument, detail: CaseDetail): Standing {
  if (!doc.analysis.matchedRequiredDocument) return 'unmatched'
  return detail.countingDocumentIds.includes(doc.id) ? 'counting' : 'superseded'
}

/**
 * Formats a browser will draw in an <img>. HEIC is deliberately not among them: an iPhone photo
 * uploads and the agent reads it without trouble, but no browser renders it, so asking for one
 * produces a broken image next to a perfectly good analysis.
 */
const BROWSERS_CAN_DRAW = ['image/png', 'image/jpeg', 'image/webp', 'image/gif', 'image/avif']

export function previewOf(doc: UploadedDocument): string | undefined {
  return BROWSERS_CAN_DRAW.includes(doc.contentType)
    ? `/api/documents/${doc.id}/file`
    : undefined
}
