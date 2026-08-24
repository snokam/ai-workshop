import type {
  ClaimOverview,
  DocumentRequest,
  UploadedDocument,
} from "../../../api";

export function Checklist({
  chosen,
  alsoSent = [],
  askedFor = [],
  askedForHeading = "Your claim handler has also asked for",
}: {
  chosen: ClaimOverview;
  alsoSent?: UploadedDocument[];
  askedFor?: DocumentRequest[];
  /** The claimant and the handler are looking at the same list from opposite sides. */
  askedForHeading?: string;
}) {
  return (
    <div className="checklist">
      <ul>
        {chosen.requiredDocuments.map((required) => {
          const outstanding = chosen.outstanding.includes(required);
          return (
            <li
              key={required}
              className={outstanding ? "outstanding" : "matched"}
            >
              <span aria-hidden>{outstanding ? "○" : "✓"}</span>
              {required}
            </li>
          );
        })}

        {alsoSent
          .filter(
            (doc, i, all) =>
              all.findIndex((d) => d.contentHash === doc.contentHash) === i,
          )
          .map((doc) => (
            <li key={doc.id} className="unasked">
              <span aria-hidden>+</span>
              <span>
                {doc.analysis.category}
                <small>
                  {doc.filename} — kept, but not one of the documents above
                </small>
              </span>
            </li>
          ))}
      </ul>

      {askedFor.length > 0 && (
        <div className="asked-for">
          <h3>{askedForHeading}</h3>
          <ul>
            {askedFor.map((request) => (
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
  );
}
