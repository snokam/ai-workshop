import type { FraudScreening, UploadedDocument } from '../../../api'
import { CONFIDENCE_LABEL, QUALITY_LABEL } from '../../../lib/labels'
import type { Standing } from '../standing'
import { Screening } from '../../task_5_fraud_detection/Screening'

export function DocumentCard({
  doc,
  preview,
  standing,
  blocking,
  screening,
  onReview,
}: {
  doc: UploadedDocument
  preview?: string
  standing?: Standing
  blocking?: boolean
  screening?: FraudScreening
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
            <span className="confidence low">Matches none of the documents this case needs — kept anyway</span>
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
              I can read this — let the case proceed
            </button>
          )}
          {doc.reviewed && <p className="reviewed">Reviewed by a case handler.</p>}
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

        {screening && screening.indicators.length > 0 && <Screening screening={screening} />}
      </div>
    </article>
  )
}
