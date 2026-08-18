import type { CaseOverview, UploadedDocument } from '../../../api'

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
