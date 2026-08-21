
import { json } from './client'

export type TaskKey =
  | 'FIRST_AGENT'
  | 'DOCUMENT_AGENT'
  | 'GUARDRAILS'
  | 'FRAUD_DETECTION'
  | 'CHAT'
  | 'SUMMARY'
  | 'CREATE_CASE_CHAT'

export interface TaskState {
  number: number
  key: TaskKey
  title: string
  file: string
  todo: string
  brief: string
  done: boolean
}

export function listTasks(): Promise<TaskState[]> {
  return fetch('/api/workshop/tasks').then((r) => json<TaskState[]>(r))
}
