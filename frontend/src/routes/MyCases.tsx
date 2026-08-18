import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { listCases } from '../api'
import type { CaseOverview } from '../api'
import { STATUS_LABEL } from '../lib/labels'
import { rememberedCaseIds } from '../lib/myCases'

/** The cases opened from this browser that the backend still has, so the claimant can return to one. */
export function MyCases() {
  const [cases, setCases] = useState<CaseOverview[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const mine = new Set(rememberedCaseIds())
    listCases()
      .then((all) => setCases(all.filter((c) => mine.has(c.id))))
      .catch((e: Error) => setError(e.message))
  }, [])

  return (
    <>
      <header>
        <h1>My cases</h1>
        <p>The cases you have opened. Pick one to see what it still needs and send more in.</p>
      </header>

      {error && (
        <p className="error" role="alert">
          {error}
        </p>
      )}

      <section className="cases">
        {cases !== null && cases.length === 0 && (
          <p className="empty">You have not opened any cases yet. Report a new case to get started.</p>
        )}
        {cases?.map((c) => (
          <Link key={c.id} className="case-row" to={`/cases/${c.id}`}>
            <span className="reference">
              {c.typeLabel}
              <span className="case-reference"> · {c.reference}</span>
            </span>
            <span className={`status ${c.status.toLowerCase()}`}>{STATUS_LABEL[c.status]}</span>
            <span className="outstanding-count">
              {c.outstanding.length === 0
                ? 'Everything required has arrived'
                : `Still needs ${c.outstanding.join(', ')}`}
            </span>
          </Link>
        ))}
      </section>
    </>
  )
}

