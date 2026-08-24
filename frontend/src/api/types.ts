
export type Quality = 'GOOD' | 'ACCEPTABLE' | 'POOR'

export type MatchConfidence = 'HIGH' | 'MEDIUM' | 'LOW'

export type ClaimStatus = 'AWAITING_DOCUMENTS' | 'NEEDS_REVIEW' | 'READY_FOR_DECISION'

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

export interface UploadedDocument {
  id: string
  claimId: string
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
  claimId: string
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

export interface ClaimOverview {
  id: string
  reference: string
  typeLabel: string
  status: ClaimStatus
  requiredDocuments: string[]
  outstanding: string[]
}

export interface CreatedClaim {
  id: string
  reference: string
  typeLabel: string
  confidence: MatchConfidence
  rationale: string
  requiredDocuments: string[]
  status: ClaimStatus
}

export interface ClaimDetail {
  overview: ClaimOverview
  documents: UploadedDocument[]
  countingDocumentIds: string[]
  blockedDocumentIds: string[]
  summary: string
  statusNote: string
  documentRequests: DocumentRequest[]
  proposals: ProposalCard[]
  conversation: ChatTurn[]
}

export interface SupportedClaimType {
  label: string
  description: string
}

/* --- optional module: conversational intake ("Report with AI chat", task 7) --- */

export interface InterviewAnswer {
  question: string
  answer: string
}

export interface InterviewResponse {
  status: 'NEEDS_INFO' | 'DECIDED'
  questions: string[]
  rationale: string | null
  createdClaim: CreatedClaim | null
}
