import { isTaskNotImplemented } from '../../../api/client'
import { useTask } from '../../../lib/task-state'
import { TaskNotDone } from '../TaskNotDone'

/**
 * Everything that can come back wrong, on one screen.
 *
 * An unfinished exercise is not a failure, so it gets the brief and the file to open rather than a
 * red message — but only if the screen was not already saying so. A gate explains before the click
 * and this explains after it, and both firing means reading the same paragraph twice. When the
 * shared state already knows the task is undone, a gate is on screen and this stays quiet.
 *
 * It speaks up when that state was wrong: the flag was changed after the page loaded, or a task
 * nothing on this screen is gated on turned out to be the one that failed.
 */
export function Failure({ error }: { error: unknown }) {
  const known = useTask(isTaskNotImplemented(error) ? error.detail.key : 'FIRST_AGENT')

  if (isTaskNotImplemented(error)) {
    const alreadyExplained = known !== null && !known.done && known.key === error.detail.key
    return alreadyExplained ? null : <TaskNotDone error={error} />
  }

  return (
    <p className="error" role="alert">
      {error instanceof Error ? error.message : String(error)}
    </p>
  )
}
