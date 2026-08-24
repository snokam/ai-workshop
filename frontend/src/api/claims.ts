
import { failure, json } from './client'
import type { DocumentRequest,
  ClaimDetail,
  ClaimOverview,
  ChatAnswer,
  CreatedClaim,
  InterviewAnswer,
  InterviewResponse,
  ProposalCard,
  SupportedClaimType,
} from './types'

export async function listClaims(): Promise<ClaimOverview[]> {
  return json(await fetch('/api/claims'))
}

export async function listClaimTypes(): Promise<SupportedClaimType[]> {
  return json(await fetch('/api/claims/types'))
}

export async function openClaim(claimId: string): Promise<ClaimDetail> {
  return json(await fetch(`/api/claims/${claimId}`))
}

export async function createClaim(description: string): Promise<CreatedClaim> {
  return json(
    await fetch('/api/claims', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ description }),
    }),
  )
}

export async function interviewIntake(
  description: string,
  answers: InterviewAnswer[],
): Promise<InterviewResponse> {
  return json(
    await fetch('/api/claims/interview', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ description, answers }),
    }),
  )
}

export async function reviewDocument(documentId: string): Promise<void> {
  const response = await fetch(`/api/documents/${documentId}/review`, { method: 'POST' })
  if (!response.ok) throw await failure(response)
}

export async function askClaimChat(claimId: string, question: string): Promise<ChatAnswer> {
  return json(
    await fetch(`/api/claims/${claimId}/chat`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ question }),
    }),
  )
}

export async function confirmProposal(proposalId: string): Promise<ProposalCard> {
  return json(await fetch(`/api/claims/proposals/${proposalId}/confirm`, { method: 'POST' }))
}

export async function declineProposal(proposalId: string): Promise<ProposalCard> {
  return json(await fetch(`/api/claims/proposals/${proposalId}/decline`, { method: 'POST' }))
}

export async function listDocumentRequests(claimId: string): Promise<DocumentRequest[]> {
  const response = await fetch(`/api/claims/${claimId}/document-requests`)
  if (!response.ok) return []
  return response.json()
}
