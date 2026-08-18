import { useEffect, useState, type ReactNode } from 'react'
import { listTasks, type TaskState } from '../../../api/workshop'
import { TasksContext } from '../../../lib/task-state'

export function TasksProvider({ children }: { children: ReactNode }) {
  const [tasks, setTasks] = useState<TaskState[] | null>(null)

  useEffect(() => {
    listTasks()
      .then(setTasks)
      .catch(() => setTasks([]))
  }, [])

  return <TasksContext.Provider value={tasks}>{children}</TasksContext.Provider>
}
