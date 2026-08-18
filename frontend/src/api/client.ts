import type { TaskKey } from './workshop'

export interface TaskNotImplemented {
  taskNotImplemented: true
  task: number
  key: TaskKey
  title: string
  file: string
  todo: string
  brief: string
}

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

export async function failureMessage(response: Response): Promise<string> {
  try {
    const body = await response.json()
    if (body && typeof body.message === 'string') return body.message
  } catch {
  }
  return `${response.status} ${response.statusText}`
}

export async function json<T>(response: Response): Promise<T> {
  if (!response.ok) throw await failure(response)
  return response.json() as Promise<T>
}

export async function failure(response: Response): Promise<Error> {
  if (response.status === 501) {
    try {
      const body = (await response.clone().json()) as TaskNotImplemented
      if (body?.taskNotImplemented) return new TaskNotImplementedError(body)
    } catch {
    }
  }
  return new Error(await failureMessage(response))
}

export interface SupportedCaseType {
  label: string
  description: string
}
