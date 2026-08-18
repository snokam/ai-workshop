import type { ReactNode } from 'react'
import type { TaskKey } from '../api/workshop'
import { useTask } from '../lib/tasks'

/**
 * A feature that is waiting on an exercise.
 *
 * The controls stay on screen. Hiding them would remove the one thing that shows what the task is
 * for — a participant needs to see the form they are about to make work, not an empty space where
 * it will eventually be. They are rendered inert instead: visible, dimmed, and not clickable, with
 * the brief underneath saying which file to open.
 *
 * Once the flag is set the wrapper disappears entirely and the feature is simply there.
 */
export function TaskGate({
  task: key,
  children,
  instead,
}: {
  task: TaskKey
  children: ReactNode
  /** One sentence on what this particular screen is missing, above the task's own todo. */
  instead?: ReactNode
}) {
  const task = useTask(key)

  // Still loading, or already written: get out of the way.
  if (task === null || task.done) return <>{children}</>

  return (
    <div className="task-pending">
      {/* inert keeps it visible and readable while taking it out of tab order and off the pointer. */}
      <div className="task-pending-preview" inert>
        {children}
      </div>

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
          The controls above are switched off until this task is written. Everything else works.
        </p>
      </section>
    </div>
  )
}
