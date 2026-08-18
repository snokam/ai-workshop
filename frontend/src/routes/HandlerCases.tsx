import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { listCases } from '../api'
import type { CaseOverview } from '../api'
import { STATUS_LABEL } from '../lib/labels'

/** Every case, cheap to read: no agent runs to produce this list. Opening one is what costs. */
export function HandlerCases() {
  const [cases, setCases] = useState<CaseOverview[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    listCases()
      .then(setCases)
      .catch((e: Error) => setError(e.message))
  }, [])

  return (
    <>
      <header>
        <h1>Cases</h1>
        <p>
          Where each case stands, worked out from the documents attached to it. Opening one runs the
          agents over it; this list does not.
        </p>
      </header>

      {error && (
        <p className="error" role="alert">
          {error}
        </p>
      )}

      <section className="cases">
        {cases.length === 0 && (
          <p className="empty">No cases yet. They appear here once someone opens one on the intake side.</p>
        )}
        {cases.map((c) => (
          <Link key={c.id} className="case-row" to={`/casehandler/cases/${c.id}`}>
            <span className="reference">
              {c.typeLabel}
              <span className="case-reference"> · {c.reference}</span>
            </span>
            <span className={`status ${c.status.toLowerCase()}`}>{STATUS_LABEL[c.status]}</span>
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
