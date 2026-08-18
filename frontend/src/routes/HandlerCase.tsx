import { useCallback, useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { openCase, reviewDocument } from '../api'
import type { CaseDetail } from '../api'
import { CaseChat } from '../components/CaseChat'
import { Checklist } from '../components/Checklist'
import { DocumentCard } from '../components/DocumentCard'
import { previewOf, standingOf } from '../lib/documents'
import { STATUS_LABEL } from '../lib/labels'

/**
 * One case, read across. Its own address, so a handler can keep it open in a tab, send it to a
 * colleague, or refresh without losing their place — none of which the old in-page state allowed.
 *
 * Opening costs two model calls, which is why the list never does it and why this shows its own
 * waiting state rather than blocking the list behind a spinner.
 */
export function HandlerCase() {
  const { caseId = '' } = useParams()
  const [detail, setDetail] = useState<CaseDetail | null>(null)
  const [error, setError] = useState<string | null>(null)

  const read = useCallback(async () => {
    setError(null)
    try {
      setDetail(await openCase(caseId))
    } catch (e) {
      setError((e as Error).message)
    }
  }, [caseId])

  useEffect(() => {
    void read()
  }, [read])

  async function review(documentId: string) {
    setError(null)
    try {
      await reviewDocument(documentId)
      await read()
    } catch (e) {
      setError((e as Error).message)
    }
  }

  if (!detail) {
    return (
      <>
        {error ? (
          <p className="error" role="alert">
            {error}
          </p>
        ) : (
          <p className="reading">
            <span className="spinner" aria-hidden />
            Reading the case…
          </p>
        )}
      </>
    )
  }

  const { overview } = detail

  return (
    <>
      <div className="with-chat">
        <div className="case-contents">
          <header>
            <h1>{overview.typeLabel}</h1>
            <p className="case-reference-line">{overview.reference}</p>
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
                preview={previewOf(doc)}
                standing={standingOf(doc, detail)}
                blocking={detail.blockedDocumentIds.includes(doc.id)}
                screening={detail.screenings.find((s) => s.documentId === doc.id)}
                onReview={() => void review(doc.id)}
              />
            ))}
          </section>
        </div>

        <CaseChat detail={detail} onCaseChanged={read} />
      </div>
    </>
  )
}
