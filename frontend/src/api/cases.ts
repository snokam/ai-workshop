/** Everything the screens ask of a Case: opening one, reading one, and answering the agent. */

import { failureMessage, json } from './client'
import type { CaseDetail, CaseOverview, ChatAnswer, CreatedCase, ProposalCard, SupportedCaseType } from './types'

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
