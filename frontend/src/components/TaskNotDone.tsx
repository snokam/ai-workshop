import type { TaskNotImplementedError } from '../api/client'

/**
 * What a screen shows when the exercise behind it has not been written yet.
 *
 * Deliberately not styled as an error: nothing has gone wrong, the workshop simply has not reached
 * this task. The file path is the point — it is the one thing a participant needs, and it is given
 * from the repository root so it can be pasted straight into an editor.
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
