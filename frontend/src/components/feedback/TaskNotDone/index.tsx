import type { TaskNotImplementedError } from '../../../api/client'

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
