
import { failure, json } from './client'
import type { DocumentRequest,
  CaseDetail,
  CaseOverview,
  ChatAnswer,
  CreatedCase,
  InterviewAnswer,
  InterviewResponse,
  ProposalCard,
  SupportedCaseType,
} from './types'

export async function listCases(): Promise<CaseOverview[]> {
  return json(await fetch('/api/cases'))
}

export async function listCaseTypes(): Promise<SupportedCaseType[]> {
  return json(await fetch('/api/cases/types'))
}

export async function openCase(caseId: string): Promise<CaseDetail> {
  return json(await fetch(`/api/cases/${caseId}`))
}

export async function createCase(description: string): Promise<CreatedCase> {
  return json(
    await fetch('/api/cases', {
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
    await fetch('/api/cases/interview', {
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

export async function listDocumentRequests(caseId: string): Promise<DocumentRequest[]> {
  const response = await fetch(`/api/cases/${caseId}/document-requests`)
  if (!response.ok) return []
  return response.json()
}
