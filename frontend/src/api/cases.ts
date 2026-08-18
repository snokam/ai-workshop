
import { failure, json } from './client'
import type { CaseDetail, CaseOverview, ChatAnswer, CreatedCase, ProposalCard, SupportedCaseType } from './types'

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

export async function reviewDocument(documentId: string): Promise<void> {
  const response = await fetch(`/api/cases/documents/${documentId}/review`, { method: 'POST' })
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
