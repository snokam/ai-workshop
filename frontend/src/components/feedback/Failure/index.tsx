import { isTaskNotImplemented } from '../../../api/client'
import { useTask } from '../../../lib/task-state'
import { TaskNotDone } from '../TaskNotDone'

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
