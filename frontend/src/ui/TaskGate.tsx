import type { ReactNode } from 'react'
import type { TaskKey } from '../api/workshop'
import { useTask } from '../workshop/task-state'

/**
 * A feature that is waiting on an exercise.
 *
 * The controls stay live. You can type in the field, press the button, drop a file — and then the
 * call fails and says which file to open. Blocking the input would teach less: half of what a task
 * is for only becomes clear when you use the thing it is missing from.
 *
 * So this only adds the explanation. The failure on use is the backstop in Failure, and the two
 * say the same thing.
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
