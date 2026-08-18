import type { CaseOverview, UploadedDocument } from '../api'

/**
 * What the case still needs. Ticked items are matched; the rest is what is left to do, and anything
 * that arrived without being asked for is listed under both.
 *
 * Anything a case handler has additionally asked for sits directly underneath, in the same card and
 * deliberately not in the same list: the checklist is what the case status is derived from, and a
 * request is a question that does not move the case either way.
 */
export function Checklist({ chosen, alsoSent = [] }: { chosen: CaseOverview; alsoSent?: UploadedDocument[] }) {
  return (
    <div className="checklist">
      <ul>
        {chosen.requiredDocuments.map((required) => {
          const outstanding = chosen.outstanding.includes(required)
          return (
            <li key={required} className={outstanding ? 'outstanding' : 'matched'}>
              <span aria-hidden>{outstanding ? '○' : '✓'}</span>
              {required}
            </li>
          )
        })}

        {/*
          What arrived that the list did not ask for. Kept in the same list rather than a section of
          its own, because the question the list answers is "where does my case stand", and a
          document nobody asked for is part of that answer — it is work the claimant has already
          done, and leaving it off the list reads as though it were never received.
        */}
        {/* One line per distinct file: sending the same thing twice is still one thing sent. */}
        {alsoSent
          .filter((doc, i, all) => all.findIndex((d) => d.contentHash === doc.contentHash) === i)
          .map((doc) => (
            <li key={doc.id} className="unasked">
              <span aria-hidden>+</span>
              <span>
                {doc.analysis.category}
                <small>{doc.filename} — kept, but not one of the documents above</small>
              </span>
            </li>
          ))}
      </ul>

      {chosen.documentRequests.length > 0 && (
        <div className="asked-for">
          <h3>Your case handler has also asked for</h3>
          <ul>
            {chosen.documentRequests.map((request) => (
              <li key={request.id}>
                <span aria-hidden>✉</span>
                <span>
                  <strong>{request.label}</strong>
                  <small>{request.reason}</small>
                </span>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  )
}

/* --- the case handler's side --------------------------------------------- */
