
import type { CaseDetail, UploadedDocument } from '../../api'

export type Standing = 'counting' | 'superseded' | 'unmatched'

export function standingOf(doc: UploadedDocument, detail: CaseDetail): Standing {
  if (!doc.analysis.matchedRequiredDocument) return 'unmatched'
  return detail.countingDocumentIds.includes(doc.id) ? 'counting' : 'superseded'
}

export function previewOf(doc: UploadedDocument): string | undefined {
  return doc.contentType.startsWith('image/') ? `/api/documents/${doc.id}/file` : undefined
}
