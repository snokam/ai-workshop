/** How every call in this folder talks to the backend, and how a failure becomes a message. */

/** Pulls the backend's `{ message }` out of a failed response so the screen can show the real cause. */
export async function failureMessage(response: Response): Promise<string> {
  try {
    const body = await response.json()
    if (body && typeof body.message === 'string') return body.message
  } catch {
    // Not JSON — fall through to the status line.
  }
  return `${response.status} ${response.statusText}`
}

export async function json<T>(response: Response): Promise<T> {
  if (!response.ok) throw new Error(await failureMessage(response))
  return response.json() as Promise<T>
}

/** One kind of insurance the system can open a case for, for the front page to list. */
export interface SupportedCaseType {
  label: string
  description: string
}
