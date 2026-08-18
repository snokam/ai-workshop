/** How every call in this folder talks to the backend, and how a failure becomes a message. */

/**
 * A task nobody has written yet. The backend answers 501 with the file to open rather than failing,
 * so an unfinished exercise stops exactly one feature instead of the whole app.
 */
export interface TaskNotImplemented {
  taskNotImplemented: true
  task: number
  title: string
  file: string
  todo: string
  brief: string
}

/** Thrown instead of a plain Error, so a screen can show the brief rather than a red message. */
export class TaskNotImplementedError extends Error {
  detail: TaskNotImplemented

  constructor(detail: TaskNotImplemented) {
    super(`Task ${detail.task} (${detail.title}) is not implemented yet`)
    this.name = 'TaskNotImplementedError'
    this.detail = detail
  }
}

export function isTaskNotImplemented(e: unknown): e is TaskNotImplementedError {
  return e instanceof TaskNotImplementedError
}

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
  if (!response.ok) throw await failure(response)
  return response.json() as Promise<T>
}

/**
 * Turns a failed response into something to throw. An unfinished task comes back as its own error
 * type carrying the file and the brief; everything else is a plain Error with the backend's message.
 */
export async function failure(response: Response): Promise<Error> {
  if (response.status === 501) {
    try {
      const body = (await response.clone().json()) as TaskNotImplemented
      if (body?.taskNotImplemented) return new TaskNotImplementedError(body)
    } catch {
      // Not the shape we expected — fall through to the ordinary message.
    }
  }
  return new Error(await failureMessage(response))
}

/** One kind of insurance the system can open a case for, for the front page to list. */
export interface SupportedCaseType {
  label: string
  description: string
}
