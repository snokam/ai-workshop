import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { listClaims } from '../../api'
import type { ClaimOverview } from '../../api'
import { STATUS_LABEL } from '../../lib/labels'
import { Failure } from '../../components/feedback/Failure'
import { TaskGate } from '../../components/workshop/TaskGate'
import { useTaskPending } from '../../lib/task-state'

export function Claims() {
  const [claims, setClaims] = useState<ClaimOverview[]>([])
  const [error, setError] = useState<Error | null>(null)
  const firstAgentPending = useTaskPending('FIRST_AGENT')

  useEffect(() => {
    listClaims()
      .then(setClaims)
      .catch((e: Error) => setError(e))
  }, [])

  return (
    <>
      <header>
        <h1>Claims</h1>
        <p>
          Where each claim stands, worked out from the documents attached to it.
          Opening one runs the agents over it; this list does not.
        </p>
      </header>

      {error && <Failure error={error} />}

      <section className="claims">
        {claims.length === 0 &&
          (firstAgentPending ? (
            <TaskGate
              task="FIRST_AGENT"
              instead="No claim can be opened yet, so there is nothing for this list to show. A claim is created by the agent that reads what a claimant typed and decides which kind of claim it is."
            >
              <p className="empty">
                No claims yet. They appear here once someone opens one on the intake side.
              </p>
            </TaskGate>
          ) : (
            <p className="empty">
              No claims yet. They appear here once someone opens one on the
              intake side.
            </p>
          ))}
        {claims.map((c) => (
          <Link
            key={c.id}
            className="claim-row"
            to={`/claimhandler/claims/${c.id}`}
          >
            <span className="reference">
              {c.typeLabel}
              <span className="claim-reference"> · {c.reference}</span>
            </span>
            <span className={`status ${c.status.toLowerCase()}`}>
              {STATUS_LABEL[c.status]}
            </span>
            <span className="outstanding-count">
              {c.outstanding.length === 0
                ? 'Everything required has arrived'
                : `Waiting for ${c.outstanding.join(', ')}`}
            </span>
          </Link>
        ))}
      </section>
    </>
  )
}
