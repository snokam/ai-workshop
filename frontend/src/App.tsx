import { useEffect, useRef, useState } from 'react'
import {
  listCases,
  openCase,
  reviewDocument,
  uploadDocument,
  type CaseDetail,
  type CaseOverview,
  type CaseStatus,
  type FraudScreening,
  type MatchConfidence,
  type Quality,
  type UploadedDocument,
} from './api'

const QUALITY_LABEL: Record<Quality, string> = {
  GOOD: 'Looks good',
  ACCEPTABLE: 'Usable, with notes',
  POOR: 'Hard to read',
}

const STATUS_LABEL: Record<CaseStatus, string> = {
  AWAITING_DOCUMENTS: 'Awaiting documents',
  NEEDS_REVIEW: 'Needs review',
  READY_FOR_DECISION: 'Ready for decision',
}

const CONFIDENCE_LABEL: Record<MatchConfidence, string> = {
  HIGH: 'confident',
  MEDIUM: 'fairly sure',
  LOW: 'unsure',
}

const INDICATOR_LABEL: Record<FraudScreening['indicators'][number]['kind'], string> = {
  ALREADY_UPLOADED: 'Sent before',
  EDITED_IN_SOFTWARE: 'Touched by an editor',
  NO_CAMERA_ORIGIN: 'No camera metadata',
  DATE_OUT_OF_PLACE: 'Capture date',
  ADDRESSED_THE_AGENT: 'Tried to instruct the agent',
}

/**
 * Two screens, one app: the claimant uploading into a case, and the case handler reading across one.
 *
 * They are deliberately not behind any login. The two roles are a vocabulary distinction here, not a
 * permission model — the workshop is about what the agents do, not about who is allowed to see it.
 */
export default function App() {
  const [side, setSide] = useState<'upload' | 'handler'>('upload')

  return (
    <main>
      <nav className="sides">
        <button className={side === 'upload' ? 'on' : ''} onClick={() => setSide('upload')}>
          Upload a document
        </button>
        <button className={side === 'handler' ? 'on' : ''} onClick={() => setSide('handler')}>
          Case handler
        </button>
      </nav>

      {side === 'upload' ? <ClaimantScreen /> : <HandlerScreen />}
    </main>
  )
}

/* --- the claimant's side ------------------------------------------------- */

function ClaimantScreen() {
  const [cases, setCases] = useState<CaseOverview[]>([])
  const [caseId, setCaseId] = useState<string>('')
  const [documents, setDocuments] = useState<UploadedDocument[]>([])
  const [busyWith, setBusyWith] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const fileInput = useRef<HTMLInputElement>(null)

  // Previews are made from the File the browser already has. The backend never stores the bytes,
  // so this map only holds documents uploaded in this tab, this session.
  const [previews, setPreviews] = useState<Record<string, string>>({})

  const chosen = cases.find((c) => c.id === caseId)

  useEffect(() => {
    listCases()
      .then((found) => {
        setCases(found)
        setCaseId((current) => current || found[0]?.id || '')
      })
      .catch((e: Error) => setError(e.message))
  }, [])

  async function handleFiles(files: FileList | null) {
    if (!files?.length || !caseId) return
    setError(null)

    for (const file of Array.from(files)) {
      setBusyWith(file.name)
      try {
        const uploaded = await uploadDocument(caseId, file)
        setDocuments((current) => [uploaded, ...current])
        if (file.type.startsWith('image/')) {
          setPreviews((current) => ({ ...current, [uploaded.id]: URL.createObjectURL(file) }))
        }
        // The case now needs one thing fewer, so re-read what is outstanding.
        setCases(await listCases())
      } catch (e) {
        setError((e as Error).message)
      } finally {
        setBusyWith(null)
      }
    }
    if (fileInput.current) fileInput.current.value = ''
  }

  return (
    <>
      <header>
        <h1>Document intake</h1>
        <p>
          Choose your case, then upload a document to it. An agent reads it, tells you what it is, says
          which of the documents your case needs it counts as, and whether the file is good enough to
          work with.
        </p>
      </header>

      <label className="picker">
        <span>Your case</span>
        <select value={caseId} onChange={(e) => setCaseId(e.target.value)}>
          {cases.map((c) => (
            <option key={c.id} value={c.id}>
              {c.reference}
            </option>
          ))}
        </select>
      </label>

      {chosen && <Checklist chosen={chosen} />}

      <label
        className={`dropzone ${busyWith ? 'busy' : ''}`}
        onDragOver={(e) => e.preventDefault()}
        onDrop={(e) => {
          e.preventDefault()
          void handleFiles(e.dataTransfer.files)
        }}
      >
        <input
          ref={fileInput}
          type="file"
          accept="application/pdf,image/*"
          multiple
          disabled={busyWith !== null || !caseId}
          onChange={(e) => void handleFiles(e.target.files)}
        />
        {busyWith ? (
          <span className="reading">
            <span className="spinner" aria-hidden />
            Reading <strong>{busyWith}</strong>…
          </span>
        ) : (
          <span>
            <strong>Drop a PDF or a photo here</strong>
            <small>or click to choose a file</small>
          </span>
        )}
      </label>

      {error && (
        <p className="error" role="alert">
          {error}
        </p>
      )}

      <section className="documents">
        {documents.length === 0 && !busyWith && <p className="empty">Nothing uploaded yet.</p>}
        {documents.map((doc) => (
          <DocumentCard key={doc.id} doc={doc} preview={previews[doc.id]} />
        ))}
      </section>
    </>
  )
}

/** What the case still needs. Ticked items are matched; the rest is what is left to do. */
function Checklist({ chosen }: { chosen: CaseOverview }) {
  return (
    <ul className="checklist">
      {chosen.requiredDocuments.map((required) => {
        const outstanding = chosen.outstanding.includes(required)
        return (
          <li key={required} className={outstanding ? 'outstanding' : 'matched'}>
            <span aria-hidden>{outstanding ? '○' : '✓'}</span>
            {required}
          </li>
        )
      })}
    </ul>
  )
}

/* --- the case handler's side --------------------------------------------- */

function HandlerScreen() {
  const [cases, setCases] = useState<CaseOverview[]>([])
  const [open, setOpen] = useState<CaseDetail | null>(null)
  const [opening, setOpening] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    listCases().then(setCases).catch((e: Error) => setError(e.message))
  }, [])

  async function show(caseId: string) {
    setError(null)
    setOpening(caseId)
    try {
      setOpen(await openCase(caseId))
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setOpening(null)
    }
  }

  async function review(documentId: string, caseId: string) {
    setError(null)
    try {
      await reviewDocument(documentId)
      setCases(await listCases())
      await show(caseId)
    } catch (e) {
      setError((e as Error).message)
    }
  }

  if (open) {
    return (
      <>
        <button className="back" onClick={() => setOpen(null)}>
          ← All cases
        </button>
        <CaseScreen detail={open} onReview={review} error={error} />
      </>
    )
  }

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
        {cases.map((c) => (
          <button key={c.id} className="case-row" onClick={() => void show(c.id)} disabled={opening !== null}>
            <span className="reference">{c.reference}</span>
            <span className={`status ${c.status.toLowerCase()}`}>{STATUS_LABEL[c.status]}</span>
            <span className="outstanding-count">
              {opening === c.id
                ? 'Reading the case…'
                : c.outstanding.length === 0
                  ? 'Everything required has arrived'
                  : `Waiting for ${c.outstanding.join(', ')}`}
            </span>
          </button>
        ))}
      </section>
    </>
  )
}

function CaseScreen({
  detail,
  onReview,
  error,
}: {
  detail: CaseDetail
  onReview: (documentId: string, caseId: string) => Promise<void>
  error: string | null
}) {
  const { overview } = detail

  return (
    <>
      <header>
        <h1>{overview.reference}</h1>
        <p className={`status ${overview.status.toLowerCase()}`}>{STATUS_LABEL[overview.status]}</p>
      </header>

      {error && (
        <p className="error" role="alert">
          {error}
        </p>
      )}

      <section className="agent-prose">
        <h2>Where this stands</h2>
        <p>{detail.statusNote}</p>
      </section>

      <Checklist chosen={overview} />

      <section className="agent-prose">
        <h2>Across the documents</h2>
        <p>{detail.summary}</p>
      </section>

      <section className="documents">
        {detail.documents.length === 0 && <p className="empty">Nothing uploaded to this case yet.</p>}
        {detail.documents.map((doc) => (
          <DocumentCard
            key={doc.id}
            doc={doc}
            standing={standingOf(doc, detail)}
            blocking={detail.blockedDocumentIds.includes(doc.id)}
            screening={detail.screenings.find((s) => s.documentId === doc.id)}
            onReview={() => void onReview(doc.id, doc.caseId)}
          />
        ))}
      </section>
    </>
  )
}

/**
 * Which of three things this document is to the case, worked out from what the backend derived.
 *
 * A document that matched no required document is neither counting nor superseded — it is attached
 * and ignored, and saying "superseded" about it would name a newer upload that does not exist.
 */
function standingOf(doc: UploadedDocument, detail: CaseDetail): Standing {
  if (!doc.analysis.matchedRequiredDocument) return 'unmatched'
  return detail.countingDocumentIds.includes(doc.id) ? 'counting' : 'superseded'
}

/* --- shared -------------------------------------------------------------- */

/** What a document is to its case. The claimant's side has no case context, so it passes none. */
type Standing = 'counting' | 'superseded' | 'unmatched'

function DocumentCard({
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
          {/* Only where a review would actually move the case — see blockedDocumentIds. */}
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

function Screening({ screening }: { screening: FraudScreening }) {
  return (
    <section className="screening">
      <h3>Worth a look</h3>
      {screening.indicators.map((indicator, index) => (
        <div key={index} className={`indicator ${indicator.weight.toLowerCase()}`}>
          <p className="what">
            <span className="kind">{INDICATOR_LABEL[indicator.kind]}</span>
            {indicator.detail}
          </p>
          {indicator.evidence.length > 0 && (
            <ul>
              {indicator.evidence.map((line) => (
                <li key={line}>{line}</li>
              ))}
            </ul>
          )}
        </div>
      ))}
    </section>
  )
}
