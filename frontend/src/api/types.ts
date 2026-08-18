
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
  contentHash: string
  analysis: DocumentAnalysis
  reviewed: boolean
}

export interface DocumentRequest {
  id: string
  caseId: string
  label: string
  reason: string
}

export type ProposalKind = 'REVIEW' | 'DOCUMENT_REQUEST'

export type ProposalState = 'PROPOSED' | 'CONFIRMED' | 'DECLINED'

export interface ProposalCard {
  id: string
  kind: ProposalKind
  subject: string
  reason: string
  state: ProposalState
}

export interface ToolCall {
  name: string
  arguments: string
}

export interface ChatTurn {
  question: string
  answer: string
  toolCalls: ToolCall[]
  proposalIds: string[]
}

export interface ChatAnswer {
  turn: ChatTurn
  proposals: ProposalCard[]
}

export interface CaseOverview {
  id: string
  reference: string
  typeLabel: string
  status: CaseStatus
  requiredDocuments: string[]
  outstanding: string[]
  documentRequests: DocumentRequest[]
}

export interface CreatedCase {
  id: string
  reference: string
  typeLabel: string
  confidence: MatchConfidence
  rationale: string
  requiredDocuments: string[]
  status: CaseStatus
}

export interface CaseDetail {
  overview: CaseOverview
  documents: UploadedDocument[]
  countingDocumentIds: string[]
  blockedDocumentIds: string[]
  summary: string
  statusNote: string
  screenings: FraudScreening[]
  proposals: ProposalCard[]
  conversation: ChatTurn[]
}

export interface SupportedCaseType {
  label: string
  description: string
}
