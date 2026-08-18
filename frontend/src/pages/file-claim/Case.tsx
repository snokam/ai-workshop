import { useEffect, useRef, useState } from 'react'
import { useLocation, useParams } from 'react-router-dom'
import { listCases, listDocuments, uploadDocument } from '../../api'
import type { CaseOverview, CreatedCase, UploadedDocument } from '../../api'
import { Checklist } from '../../components/case/Checklist'
import { DocumentCard } from '../../components/case/DocumentCard'
import { CONFIDENCE_LABEL } from '../../lib/labels'
import { previewOf } from '../../components/case/standing'
import { Loader } from '../../components/feedback/Loader'
import { Failure } from '../../components/feedback/Failure'
import { TaskGate } from '../../components/workshop/TaskGate'

/** One case, open for uploading into. Its address is /cases/:caseId, so it survives a refresh. */
export function Case() {
  const { caseId = '' } = useParams()
  // Shown once, on arrival from the describe form. A link someone shares carries no state, so
  // opening the same address later shows the case without the "we have opened this for you" banner.
  const intro = (useLocation().state as { intro?: CreatedCase } | null)?.intro
  const [overview, setOverview] = useState<CaseOverview | null>(null)
  const [documents, setDocuments] = useState<UploadedDocument[]>([])
  const [busyWith, setBusyWith] = useState<string | null>(null)
  const [error, setError] = useState<Error | null>(null)
  const fileInput = useRef<HTMLInputElement>(null)

  // Previews are made from the File the browser already has. The backend never stores the bytes,
  // so this map only holds documents uploaded in this tab, this session.

  // The checklist needs `outstanding`, which the case list is the source of truth for. Until the
  // first read comes back, fall back to what we already know: the fresh case's list, or nothing.
  const checklist: CaseOverview =
    overview ??
    (intro
      ? {
          id: intro.id,
          reference: intro.reference,
          typeLabel: intro.typeLabel,
          status: intro.status,
          requiredDocuments: intro.requiredDocuments,
          outstanding: intro.requiredDocuments,
          documentRequests: [],
        }
      : {
          id: caseId,
          reference: '',
          typeLabel: '',
          status: 'AWAITING_DOCUMENTS',
          requiredDocuments: [],
          outstanding: [],
          documentRequests: [],
        })

  async function refreshOverview() {
    const all = await listCases()
    setOverview(all.find((c) => c.id === caseId) ?? null)
  }

  useEffect(() => {
    function refresh() {
      refreshOverview().catch((e: Error) => setError(e))
    }

    refresh()
    // Show what has already been sent to this case. The bytes are never served back, so these have
    // no preview.
    listDocuments()
      .then((all) => setDocuments(all.filter((d) => d.caseId === caseId)))
      .catch((e: Error) => setError(e))

    // The case list is a pure lookup with no model call behind it, which is what makes polling it
    // reasonable: it is how a document request a case handler has just confirmed appears here
    // without the claimant reloading the page.
    const polling = setInterval(refresh, 5000)
    return () => clearInterval(polling)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [caseId])

  async function handleFiles(files: FileList | null) {
    if (!files?.length) return
    setError(null)

    for (const file of Array.from(files)) {
      setBusyWith(file.name)
      try {
        const uploaded = await uploadDocument(caseId, file)
        setDocuments((current) => [uploaded, ...current])
        // The case now needs one thing fewer, so re-read what is outstanding.
        await refreshOverview()
      } catch (e) {
        setError(e as Error)
      } finally {
        setBusyWith(null)
      }
    }
    if (fileInput.current) fileInput.current.value = ''
  }

  return (
    <>
      {intro ? (
        <section className={`detected ${intro.confidence.toLowerCase()}`}>
          <span className="detected-label">We have opened a case for you</span>
          <h1>{intro.typeLabel}</h1>
          <p className="reference">{intro.reference}</p>
          <p className="rationale">
            {intro.rationale}{' '}
            <span className="confidence-note">
              — the agent is {CONFIDENCE_LABEL[intro.confidence]}
            </span>
          </p>
        </section>
      ) : (
        <header>
          <h1>{checklist.typeLabel || 'Your case'}</h1>
          <p className="case-reference-line">{checklist.reference}</p>
        </header>
      )}

      <header>
        <h2>What to send in</h2>
        <p>
          Upload each of these and an agent reads it, tells you what it is, says
          which item it counts as, and whether the file is good enough to work
          with.
        </p>
      </header>

      {checklist.requiredDocuments.length > 0 ? (
        <Checklist
          chosen={checklist}
          alsoSent={documents.filter(
            (d) => !d.analysis.matchedRequiredDocument,
          )}
        />
      ) : (
        <p className="empty">
          This case has no set list of documents. Send in anything relevant.
        </p>
      )}

      <TaskGate
        task="DOCUMENT_AGENT"
        instead="A file can be dropped here, but the agent that reads an uploaded PDF or photo has not been written yet."
      >
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
            disabled={busyWith !== null}
            onChange={(e) => void handleFiles(e.target.files)}
          />
          {busyWith ? (
            <span className="reading">
              <Loader />
              Reading <strong>{busyWith}</strong>…
            </span>
          ) : (
            <span>
              <strong>Drop a PDF or a photo here</strong>
              <small>or click to choose a file</small>
            </span>
          )}
        </label>
      </TaskGate>

      {error && <Failure error={error} />}

      <section className="documents">
        {documents.length === 0 && !busyWith && (
          <p className="empty">Nothing uploaded yet.</p>
        )}
        {documents.map((doc) => (
          <DocumentCard key={doc.id} doc={doc} preview={previewOf(doc)} />
        ))}
      </section>
    </>
  )
}
