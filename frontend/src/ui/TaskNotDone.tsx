import type { TaskNotImplementedError } from '../api/client'

/**
 * What a screen shows when a call comes back saying the exercise behind it is unfinished.
 *
 * The backstop to {@link TaskGate}: the gate knows before the click, this handles the case where
 * the state was read a moment before someone changed a flag. Same panel either way, so a
 * participant sees one thing, not two.
 */
export function TaskNotDone({ error }: { error: TaskNotImplementedError }) {
  const { task, title, file, todo, brief } = error.detail

  return (
    <section className="task-todo" role="status">
      <p className="task-todo-label">
        Task {task} — {title}
      </p>
      <p>{todo}</p>
      <dl>
        <dt>Open</dt>
        <dd>
          <code>{file}</code>
        </dd>
        <dt>Brief</dt>
        <dd>
          <code>{brief}</code>
        </dd>
      </dl>
      <p className="task-todo-note">
        Everything else still works. Only what this task provides is waiting on you.
      </p>
    </section>
  )
}
