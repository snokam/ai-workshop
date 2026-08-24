
import { json } from './client'
import type { UploadedDocument } from './types'

export async function listDocuments(): Promise<UploadedDocument[]> {
  return json(await fetch('/api/documents'))
}

export async function uploadDocument(claimId: string, file: File): Promise<UploadedDocument> {
  const body = new FormData()
  body.append('claimId', claimId)
  body.append('file', file)

  return json(await fetch('/api/documents', { method: 'POST', body }))
}
