import { createContext, useContext } from 'react'
import type { TaskKey, TaskState } from '../api/workshop'

export const TasksContext = createContext<TaskState[] | null>(null)

export function useTask(key: TaskKey): TaskState | null {
  const tasks = useContext(TasksContext)
  return tasks?.find((task) => task.key === key) ?? null
}

export function useTaskPending(key: TaskKey): boolean {
  const task = useTask(key)
  return task !== null && !task.done
}
