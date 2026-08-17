/**
 * The API surface, and the types that mirror the Java records on the other side.
 *
 * Keeping these in one file means there is exactly one place to change when the backend's
 * DocumentAnalysis grows a field.
 */

export type Quality = 'GOOD' | 'ACCEPTABLE' | 'POOR'

export type MatchConfidence = 'HIGH' | 'MEDIUM' | 'LOW'

export type CaseStatus = 'AWAITING_DOCUMENTS' | 'NEEDS_REVIEW' | 'READY_FOR_DECISION'

export interface ExtractedField {
  name: string
  value: string
}

export interface QualityAssessment {
  verdict: Quality
  reason: string
  issues: string[]
}

export interface DocumentAnalysis {
  category: string
  summary: string
  fields: ExtractedField[]
  /** The required document this file satisfied, or null when it satisfied none. */
  matchedRequiredDocument: string | null
  matchConfidence: MatchConfidence
  quality: QualityAssessment
}

export interface FraudIndicator {
  kind:
    | 'ALREADY_UPLOADED'
    | 'EDITED_IN_SOFTWARE'
    | 'NO_CAMERA_ORIGIN'
    | 'DATE_OUT_OF_PLACE'
    | 'ADDRESSED_THE_AGENT'
  weight: 'NOTE' | 'CONCERN' | 'STRONG'
  detail: string
  evidence: string[]
}

export interface FraudScreening {
  documentId: string
  indicators: FraudIndicator[]
}

export interface UploadedDocument {
  id: string
  caseId: string
  filename: string
  contentType: string
  sizeBytes: number
  uploadedAt: string
  analysis: DocumentAnalysis
  reviewed: boolean
}

/** One row of the case list. Cheap to fetch — no agent runs to produce it. */
export interface CaseOverview {
  id: string
  reference: string
  status: CaseStatus
  requiredDocuments: string[]
  outstanding: string[]
}

/** One case, opened. Costs two model calls, so only ever fetched for the case being looked at. */
export interface CaseDetail {
  overview: CaseOverview
  documents: UploadedDocument[]
  /**
   * Which documents the status was derived from — the newest match for each required document. An
   * attached document not listed here is either superseded by a newer upload of the same required
   * document, or matched none of them; `matchedRequiredDocument` tells those two apart.
   */
  countingDocumentIds: string[]
  /** The documents holding the case at NEEDS_REVIEW — the only ones a review changes anything for. */
  blockedDocumentIds: string[]
  summary: string
  statusNote: string
  screenings: FraudScreening[]
}

/** Pulls the backend's `{ message }` out of a failed response so the screen can show the real cause. */
async function failureMessage(response: Response): Promise<string> {
  try {
    const body = await response.json()
    if (body && typeof body.message === 'string') return body.message
  } catch {
    // Not JSON — fall through to the status line.
  }
  return `${response.status} ${response.statusText}`
}

async function json<T>(response: Response): Promise<T> {
  if (!response.ok) throw new Error(await failureMessage(response))
  return response.json() as Promise<T>
}

export async function listCases(): Promise<CaseOverview[]> {
  return json(await fetch('/api/cases'))
}

export async function openCase(caseId: string): Promise<CaseDetail> {
  return json(await fetch(`/api/cases/${caseId}`))
}

export async function reviewDocument(documentId: string): Promise<void> {
  const response = await fetch(`/api/cases/documents/${documentId}/review`, { method: 'POST' })
  if (!response.ok) throw new Error(await failureMessage(response))
}

export async function listDocuments(): Promise<UploadedDocument[]> {
  return json(await fetch('/api/documents'))
}

export async function uploadDocument(caseId: string, file: File): Promise<UploadedDocument> {
  const body = new FormData()
  body.append('caseId', caseId)
  body.append('file', file)

  return json(await fetch('/api/documents', { method: 'POST', body }))
}
