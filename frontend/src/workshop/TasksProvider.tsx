import { useEffect, useState, type ReactNode } from 'react'
import { listTasks, type TaskState } from '../api/workshop'
import { TasksContext } from './task-state'

/**
 * Reads how far the workshop has got, once, for every screen.
 *
 * Without this a screen only discovers a task is unfinished by calling the API and catching the
 * failure, so the explanation always arrives after a click. Knowing up front lets a screen show the
 * controls a task will bring to life and say what is missing, which is the difference between a
 * dead end and an instruction.
 */
export function TasksProvider({ children }: { children: ReactNode }) {
  const [tasks, setTasks] = useState<TaskState[] | null>(null)

  useEffect(() => {
    listTasks()
      .then(setTasks)
      // A backend that is not running is its own, louder problem; the screens will say so.
      .catch(() => setTasks([]))
  }, [])

  return <TasksContext.Provider value={tasks}>{children}</TasksContext.Provider>
}
