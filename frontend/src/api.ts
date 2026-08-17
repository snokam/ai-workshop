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

/**
 * Something a case handler has asked the claimant for. Deliberately not one of the case's required
 * documents — that list is what the case status is derived from, and a question must not move a case.
 */
export interface DocumentRequest {
  id: string
  caseId: string
  label: string
  reason: string
}

export type ProposalKind = 'REVIEW' | 'DOCUMENT_REQUEST'

export type ProposalState = 'PROPOSED' | 'CONFIRMED' | 'DECLINED'

/**
 * Something the case chat agent suggested. It has performed nothing: only a case handler's click on
 * Confirm turns one into a write.
 */
export interface ProposalCard {
  id: string
  kind: ProposalKind
  /** A document's filename, or the label to ask the claimant for. */
  subject: string
  reason: string
  state: ProposalState
}

/** One thing the agent looked up while answering. Shown so a looked-up fact reads as one. */
export interface ToolCall {
  name: string
  arguments: string
}

/** One exchange. Proposals are referenced by id and resolved against the case's live list. */
export interface ChatTurn {
  question: string
  answer: string
  toolCalls: ToolCall[]
  proposalIds: string[]
}

export interface ChatAnswer {
  turn: ChatTurn
  /** Every proposal on the case, in whatever state it is now — not only the ones this turn raised. */
  proposals: ProposalCard[]
}

/** One row of the case list. Cheap to fetch — no agent runs to produce it. */
export interface CaseOverview {
  id: string
  reference: string
  /** The kind of case, e.g. "Travel insurance claim" — so the list reads as claims, not numbers. */
  typeLabel: string
  status: CaseStatus
  requiredDocuments: string[]
  outstanding: string[]
  documentRequests: DocumentRequest[]
}

/**
 * A case just opened from a free-text description. The `typeLabel`, `confidence` and `rationale`
 * are the classifier's account of why this kind of case — shown once, at creation, and not stored
 * on the case afterwards.
 */
export interface CreatedCase {
  id: string
  reference: string
  typeLabel: string
  confidence: MatchConfidence
  rationale: string
  requiredDocuments: string[]
  status: CaseStatus
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
  proposals: ProposalCard[]
  /** The case chat so far. Free to fetch — the turns were written when they were answered. */
  conversation: ChatTurn[]
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

/** One kind of insurance the system can open a case for, for the front page to list. */
export interface SupportedCaseType {
  label: string
  description: string
}

export async function listCases(): Promise<CaseOverview[]> {
  return json(await fetch('/api/cases'))
}

/** The insurance types the classifier can land on — shown on the front page so people know the scope. */
export async function listCaseTypes(): Promise<SupportedCaseType[]> {
  return json(await fetch('/api/cases/types'))
}

export async function openCase(caseId: string): Promise<CaseDetail> {
  return json(await fetch(`/api/cases/${caseId}`))
}

/** Opens a case from what the claimant typed. One classifier call runs on the backend. */
export async function createCase(description: string): Promise<CreatedCase> {
  return json(
    await fetch('/api/cases', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ description }),
    }),
  )
}

export async function reviewDocument(documentId: string): Promise<void> {
  const response = await fetch(`/api/cases/documents/${documentId}/review`, { method: 'POST' })
  if (!response.ok) throw new Error(await failureMessage(response))
}

/** One turn of the case chat. Blocks for a model call, and for any tool it decides to reach for. */
export async function askCaseChat(caseId: string, question: string): Promise<ChatAnswer> {
  return json(
    await fetch(`/api/cases/${caseId}/chat`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ question }),
    }),
  )
}

export async function confirmProposal(proposalId: string): Promise<ProposalCard> {
  return json(await fetch(`/api/cases/proposals/${proposalId}/confirm`, { method: 'POST' }))
}

export async function declineProposal(proposalId: string): Promise<ProposalCard> {
  return json(await fetch(`/api/cases/proposals/${proposalId}/decline`, { method: 'POST' }))
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
