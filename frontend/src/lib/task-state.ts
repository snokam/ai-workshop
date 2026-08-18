import { createContext, useContext } from 'react'
import type { TaskKey, TaskState } from '../api/workshop'

/**
 * Which exercises have been written, read once by {@link TasksProvider} and shared from here.
 *
 * The context and its hooks live apart from the provider because a module that exports both a
 * component and plain functions cannot be hot-reloaded.
 */
export const TasksContext = createContext<TaskState[] | null>(null)

/** The task behind a feature, or null while the list is still loading. */
export function useTask(key: TaskKey): TaskState | null {
  const tasks = useContext(TasksContext)
  return tasks?.find((task) => task.key === key) ?? null
}

/** True only when we know the task is unfinished — never while the answer is still unknown. */
export function useTaskPending(key: TaskKey): boolean {
  const task = useTask(key)
  return task !== null && !task.done
}
