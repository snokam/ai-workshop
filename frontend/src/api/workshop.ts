/** How far the workshop has got, as the screens see it. */

import { json } from './client'

/** The six exercises, in the order they are done. The key is stable; the number is presentation. */
export type TaskKey =
  | 'FIRST_AGENT'
  | 'DOCUMENT_AGENT'
  | 'GUARDRAILS'
  | 'POSTPROCESSING'
  | 'CHAT'
  | 'SUMMARY'

export interface TaskState {
  number: number
  key: TaskKey
  title: string
  /** The file to open, from the repository root. */
  file: string
  todo: string
  brief: string
  done: boolean
}

export function listTasks(): Promise<TaskState[]> {
  return fetch('/api/workshop/tasks').then((r) => json<TaskState[]>(r))
}
