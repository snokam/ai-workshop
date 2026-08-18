import type { ReactNode } from 'react'
import type { TaskKey } from '../../../api/workshop'
import { useTask } from '../../../lib/task-state'

export function TaskGate({
  task: key,
  children,
  instead,
}: {
  task: TaskKey
  children: ReactNode
  instead?: ReactNode
}) {
  const task = useTask(key)

  if (task === null || task.done) return <>{children}</>

  return (
    <div className="task-pending">
      {children}

      <section className="task-todo" role="status">
        <p className="task-todo-label">
          Task {task.number} — {task.title}
        </p>
        {instead && <p>{instead}</p>}
        <p>{task.todo}</p>
        <dl>
          <dt>Open</dt>
          <dd>
            <code>{task.file}</code>
          </dd>
          <dt>Brief</dt>
          <dd>
            <code>{task.brief}</code>
          </dd>
        </dl>
        <p className="task-todo-note">
          The controls above still work — using them is how you see what is missing. Everything
          else in the app is unaffected.
        </p>
      </section>
    </div>
  )
}
