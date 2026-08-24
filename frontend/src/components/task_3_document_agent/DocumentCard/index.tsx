import type { UploadedDocument } from '../../../api'
import { CONFIDENCE_LABEL, QUALITY_LABEL } from '../../../lib/labels'
import type { Standing } from '../standing'

export function DocumentCard({
  doc,
  preview,
  standing,
  blocking,
  onReview,
}: {
  doc: UploadedDocument
  preview?: string
  standing?: Standing
  blocking?: boolean
  onReview?: () => void
}) {
  const { analysis } = doc
  const { quality } = analysis

  return (
    <article className={`document ${standing === 'superseded' ? 'superseded' : ''}`}>
      {preview && <img className="preview" src={preview} alt="" />}

      <div className="body">
        <div className="title">
          <h2>{doc.filename}</h2>
          <span className="category">{analysis.category}</span>
        </div>

        <p className="match">
          {!analysis.matchedRequiredDocument ? (
            <span className="confidence low">Matches none of the documents this claim needs — kept anyway</span>
          ) : standing === 'superseded' ? (
            <>
              Sent as <strong>{analysis.matchedRequiredDocument}</strong>{' '}
              <span className="confidence">— a later upload is the one that counts</span>
            </>
          ) : (
            <>
              Counts as <strong>{analysis.matchedRequiredDocument}</strong>{' '}
              <span className={`confidence ${analysis.matchConfidence.toLowerCase()}`}>
                — the agent is {CONFIDENCE_LABEL[analysis.matchConfidence]}
              </span>
            </>
          )}
        </p>

        <div className={`quality ${quality.verdict.toLowerCase()}`}>
          <strong>{QUALITY_LABEL[quality.verdict]}</strong>
          <p>{quality.reason}</p>
          {quality.issues.length > 0 && (
            <ul>
              {quality.issues.map((issue) => (
                <li key={issue}>{issue}</li>
              ))}
            </ul>
          )}
          {onReview && blocking && (
            <button className="review" onClick={onReview}>
              I can read this — let the claim proceed
            </button>
          )}
          {doc.reviewed && <p className="reviewed">Reviewed by a claim handler.</p>}
        </div>

        <p className="summary">{analysis.summary}</p>

        {analysis.fields.length > 0 && (
          <dl className="fields">
            {analysis.fields.map((field) => (
              <div key={field.name}>
                <dt>{field.name}</dt>
                <dd>{field.value}</dd>
              </div>
            ))}
          </dl>
        )}
      </div>
    </article>
  )
}
