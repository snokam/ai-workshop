import { isTaskNotImplemented } from '../api/client'
import { TaskNotDone } from './TaskNotDone'

/**
 * Everything that can come back wrong, on one screen.
 *
 * An unfinished exercise is not a failure — it gets the brief and the file to open. Anything else
 * gets the backend's own message, because in a workshop the real cause is more use than a polite
 * one.
 */
export function Failure({ error }: { error: unknown }) {
  if (isTaskNotImplemented(error)) return <TaskNotDone error={error} />
  return (
    <p className="error" role="alert">
      {error instanceof Error ? error.message : String(error)}
    </p>
  )
}
