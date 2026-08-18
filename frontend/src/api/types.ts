/**
 * The types that mirror the Java records on the other side.
 *
 * One file, so there is exactly one place to change when a record over there grows a component.
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
  /** Fingerprint of the bytes. Two uploads of one file share it. */
  contentHash: string
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
  screenings: FraudScreening[]
  proposals: ProposalCard[]
  /** The case chat so far. Free to fetch — the turns were written when they were answered. */
  conversation: ChatTurn[]
}

/** One kind of insurance the system can open a case for, for the front page to list. */
export interface SupportedCaseType {
  label: string
  description: string
}
