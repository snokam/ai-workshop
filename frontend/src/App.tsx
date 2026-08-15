import { useEffect, useRef, useState } from 'react'
import { listDocuments, uploadDocument, type Quality, type UploadedDocument } from './api'

const QUALITY_LABEL: Record<Quality, string> = {
  GOOD: 'Looks good',
  ACCEPTABLE: 'Usable, with notes',
  POOR: 'Hard to read',
}

/**
 * One screen: drop a file, watch the intake agent read it, see what it found.
 *
 * Uploads are never rejected — a poor-quality file lands in the list like any other, with the
 * agent's warning attached to it.
 */
export default function App() {
  const [documents, setDocuments] = useState<UploadedDocument[]>([])
  const [busyWith, setBusyWith] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const fileInput = useRef<HTMLInputElement>(null)

  // Previews are made from the File the browser already has. The backend never stores the bytes,
  // so this map only holds documents uploaded in this tab, this session.
  const [previews, setPreviews] = useState<Record<string, string>>({})

  useEffect(() => {
    listDocuments().then(setDocuments).catch((e: Error) => setError(e.message))
  }, [])

  async function handleFiles(files: FileList | null) {
    if (!files?.length) return
    setError(null)

    for (const file of Array.from(files)) {
      setBusyWith(file.name)
      try {
        const uploaded = await uploadDocument(file)
        setDocuments((current) => [uploaded, ...current])
        if (file.type.startsWith('image/')) {
          setPreviews((current) => ({ ...current, [uploaded.id]: URL.createObjectURL(file) }))
        }
      } catch (e) {
        setError((e as Error).message)
      } finally {
        setBusyWith(null)
      }
    }
    if (fileInput.current) fileInput.current.value = ''
  }

  return (
    <main>
      <header>
        <h1>Document intake</h1>
        <p>
          Upload a document to your case. An agent reads it, tells you what it is, and says whether the
          file is good enough to work with.
        </p>
      </header>

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
    </main>
  )
}

function DocumentCard({ doc, preview }: { doc: UploadedDocument; preview?: string }) {
  const { analysis } = doc
  const { quality } = analysis

  return (
    <article className="document">
      {preview && <img className="preview" src={preview} alt="" />}

      <div className="body">
        <div className="title">
          <h2>{doc.filename}</h2>
          <span className="category">{analysis.category}</span>
        </div>

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
