import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import { listTasks, type TaskKey, type TaskState } from '../api/workshop'

/**
 * Which exercises have been written, read once and shared by every screen.
 *
 * Without this a screen only discovers a task is unfinished by calling the API and catching the
 * failure, so the explanation always arrives after a click. Knowing up front lets a screen disable
 * what cannot work yet and say why, which is the difference between a dead end and an instruction.
 *
 * The 501 handling stays as the backstop: this can be stale by one edit, the response never is.
 */
const TasksContext = createContext<TaskState[] | null>(null)

export function TasksProvider({ children }: { children: ReactNode }) {
  const [tasks, setTasks] = useState<TaskState[] | null>(null)

  useEffect(() => {
    listTasks()
      .then(setTasks)
      // A workshop backend that is not running is its own, louder problem; the screens will say so.
      .catch(() => setTasks([]))
  }, [])

  return <TasksContext.Provider value={tasks}>{children}</TasksContext.Provider>
}

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
