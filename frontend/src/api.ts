/**
 * The API surface, and the types that mirror the Java records on the other side.
 *
 * Keeping these in one file means there is exactly one place to change when the backend's
 * DocumentAnalysis grows a field.
 */

export type Quality = 'GOOD' | 'ACCEPTABLE' | 'POOR'

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
  quality: QualityAssessment
}

export interface UploadedDocument {
  id: string
  filename: string
  contentType: string
  sizeBytes: number
  uploadedAt: string
  analysis: DocumentAnalysis
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

export async function listDocuments(): Promise<UploadedDocument[]> {
  const response = await fetch('/api/documents')
  if (!response.ok) throw new Error(await failureMessage(response))
  return response.json()
}

export async function uploadDocument(file: File): Promise<UploadedDocument> {
  const body = new FormData()
  body.append('file', file)

  const response = await fetch('/api/documents', { method: 'POST', body })
  if (!response.ok) throw new Error(await failureMessage(response))
  return response.json()
}
