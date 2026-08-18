import { useEffect, useRef, useState } from 'react'
import {
  askCaseChat,
  confirmProposal,
  createCase,
  declineProposal,
  listCaseTypes,
  listCases,
  listDocuments,
  openCase,
  reviewDocument,
  uploadDocument,
  type CaseDetail,
  type CaseOverview,
  type CaseStatus,
  type ChatTurn,
  type CreatedCase,
  type FraudScreening,
  type MatchConfidence,
  type ProposalCard,
  type Quality,
  type SupportedCaseType,
  type ToolCall,
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
 * What each tool call reads as under an answer. The point is that an audience — and a case handler —
 * can see the agent doing something rather than appearing to be a chatbot with a large prompt.
 */
const TOOL_LABEL: Record<string, string> = {
  documentDetail: 'Looked up',
  readDocument: 'Read the file',
  proposeReview: 'Suggested reviewing',
  proposeDocumentRequest: 'Suggested asking for',
}

/** Three questions worth asking, so an empty chat is not a blank box. The third provokes a tool call. */
const SUGGESTED_QUESTIONS = [
  'What is this case waiting on?',
  'Do any of the documents disagree with each other?',
  'Look at the poorest scan again — what can you make out?',
]

/**
 * Two audiences, one app, split by URL rather than a toggle: the claimant on `/`, the case handler
 * on `/casehandler`. There is no login and no cross-navigation between the two — each side is simply
 * its own address. The roles are a vocabulary distinction here, not a permission model.
 */
const HANDLER_PATH = '/casehandler'

/**
 * A router small enough to not be a dependency: the two sides are two URLs, so the address bar is
 * the source of truth for which one shows. Vite serves index.html for any path in dev, so a hard
 * refresh on /casehandler lands here too.
 */
function usePathname() {
  const [path, setPath] = useState(() => window.location.pathname)

  useEffect(() => {
    const onPop = () => setPath(window.location.pathname)
    window.addEventListener('popstate', onPop)
    return () => window.removeEventListener('popstate', onPop)
  }, [])

  return path
}

export default function App() {
  const onHandler = usePathname().replace(/\/+$/, '') === HANDLER_PATH

  return <main>{onHandler ? <HandlerScreen /> : <ClaimantScreen />}</main>
}

/* --- the claimant's side ------------------------------------------------- */

/**
 * There is no login, so "my cases" cannot mean cases belonging to an account. It means the ones
 * opened from this browser: their ids are remembered here and matched against what the backend still
 * has, so a restart (the store is in-memory) quietly empties the list rather than showing stale rows.
 */
const MY_CASES_KEY = 'myCaseIds'

function rememberedCaseIds(): string[] {
  try {
    const stored = JSON.parse(localStorage.getItem(MY_CASES_KEY) ?? '[]')
    return Array.isArray(stored) ? (stored as string[]) : []
  } catch {
    return []
  }
}

function rememberCase(id: string) {
  const ids = rememberedCaseIds()
  if (!ids.includes(id)) localStorage.setItem(MY_CASES_KEY, JSON.stringify([id, ...ids]))
}

/** What the claimant is looking at: the two entry points, or one case opened for upload. */
type ClaimantView = { mode: 'new' } | { mode: 'mine' } | { mode: 'case'; caseId: string; intro?: CreatedCase }

function ClaimantScreen() {
  const [view, setView] = useState<ClaimantView>({ mode: 'new' })
  // Where "← back" from a case returns to: whichever entry point led into it.
  const [cameFrom, setCameFrom] = useState<'new' | 'mine'>('new')

  if (view.mode === 'case') {
    return (
      <CaseIntakeScreen
        caseId={view.caseId}
        intro={view.intro}
        backLabel={cameFrom === 'mine' ? '← My cases' : '← Describe a different case'}
        onBack={() => setView({ mode: cameFrom })}
      />
    )
  }

  return (
    <>
      <nav className="claimant-nav">
        <button className={view.mode === 'new' ? 'on' : ''} onClick={() => setView({ mode: 'new' })}>
          Report a new case
        </button>
        <button className={view.mode === 'mine' ? 'on' : ''} onClick={() => setView({ mode: 'mine' })}>
          My cases
        </button>
      </nav>

      {view.mode === 'new' ? (
        <DescribeCase
          onCreated={(created) => {
            rememberCase(created.id)
            setCameFrom('new')
            setView({ mode: 'case', caseId: created.id, intro: created })
          }}
        />
      ) : (
        <MyCases
          onOpen={(caseId) => {
            setCameFrom('mine')
            setView({ mode: 'case', caseId })
          }}
        />
      )}
    </>
  )
}

/** The cases opened from this browser that the backend still has, so the claimant can return to one. */
function MyCases({ onOpen }: { onOpen: (caseId: string) => void }) {
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
          <button key={c.id} className="case-row" onClick={() => onOpen(c.id)}>
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
          </button>
        ))}
      </section>
    </>
  )
}

/** The opening screen: one box where the claimant says, in their own words, what they need help with. */
function DescribeCase({ onCreated }: { onCreated: (created: CreatedCase) => void }) {
  const [description, setDescription] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [types, setTypes] = useState<SupportedCaseType[]>([])

  // Pulled from the backend so the scope shown here is exactly what the classifier can land on.
  useEffect(() => {
    let live = true
    listCaseTypes()
      .then((t) => live && setTypes(t))
      .catch(() => {})
    return () => {
      live = false
    }
  }, [])

  async function submit() {
    const text = description.trim()
    if (!text || submitting) return
    setSubmitting(true)
    setError(null)
    try {
      onCreated(await createCase(text))
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <>
      <header>
        <h1>What do you need help with?</h1>
        <p>
          Describe your situation in your own words. An agent reads it, works out what kind of case to
          open, and tells you which documents you will need to send in.
        </p>
      </header>

      <form
        className="describe"
        onSubmit={(e) => {
          e.preventDefault()
          void submit()
        }}
      >
        <textarea
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          placeholder="For example: my suitcase never turned up after my flight home, and I had to buy clothes and toiletries."
          rows={5}
          disabled={submitting}
          // Submit on Enter, newline on Shift+Enter — the box is for a sentence or two, not an essay.
          onKeyDown={(e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
              e.preventDefault()
              void submit()
            }
          }}
        />
        <button type="submit" disabled={submitting || description.trim().length === 0}>
          {submitting ? (
            <span className="reading">
              <span className="spinner" aria-hidden />
              Reading what you wrote…
            </span>
          ) : (
            'Get started'
          )}
        </button>
      </form>

      {error && (
        <p className="error" role="alert">
          {error}
        </p>
      )}

      {types.length > 0 && (
        <section className="supported">
          <h2>Insurance we can help with</h2>
          <ul>
            {types.map((type) => (
              <li key={type.label}>
                <span className="supported-label">{type.label}</span>
                <span className="supported-desc">{type.description}</span>
              </li>
            ))}
          </ul>
        </section>
      )}
    </>
  )
}

/**
 * One case, ready to receive documents: its checklist, then the upload flow into it. Reached two
 * ways — straight after describing a new case (with `intro`, so the detected type is shown), or from
 * "my cases" (without, so it opens on the plain header). Already-uploaded documents are loaded so a
 * returning claimant sees what they have sent.
 */
function CaseIntakeScreen({
  caseId,
  intro,
  backLabel,
  onBack,
}: {
  caseId: string
  intro?: CreatedCase
  backLabel: string
  onBack: () => void
}) {
  const [overview, setOverview] = useState<CaseOverview | null>(null)
  const [documents, setDocuments] = useState<UploadedDocument[]>([])
  const [busyWith, setBusyWith] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const fileInput = useRef<HTMLInputElement>(null)

  // Previews are made from the File the browser already has. The backend never stores the bytes,
  // so this map only holds documents uploaded in this tab, this session.
  const [previews, setPreviews] = useState<Record<string, string>>({})

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
      refreshOverview().catch((e: Error) => setError(e.message))
    }

    refresh()
    // Show what has already been sent to this case. The bytes are never served back, so these have
    // no preview.
    listDocuments()
      .then((all) => setDocuments(all.filter((d) => d.caseId === caseId)))
      .catch((e: Error) => setError(e.message))

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
        if (file.type.startsWith('image/')) {
          setPreviews((current) => ({ ...current, [uploaded.id]: URL.createObjectURL(file) }))
        }
        // The case now needs one thing fewer, so re-read what is outstanding.
        await refreshOverview()
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
      <button className="back" onClick={onBack}>
        {backLabel}
      </button>

      {intro ? (
        <section className={`detected ${intro.confidence.toLowerCase()}`}>
          <span className="detected-label">We have opened a case for you</span>
          <h1>{intro.typeLabel}</h1>
          <p className="reference">{intro.reference}</p>
          <p className="rationale">
            {intro.rationale}{' '}
            <span className="confidence-note">— the agent is {CONFIDENCE_LABEL[intro.confidence]}</span>
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
          Upload each of these and an agent reads it, tells you what it is, says which item it counts
          as, and whether the file is good enough to work with.
        </p>
      </header>

      {checklist.requiredDocuments.length > 0 ? (
        <Checklist chosen={checklist} />
      ) : (
        <p className="empty">This case has no set list of documents. Send in anything relevant.</p>
      )}

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
    </>
  )
}

/**
 * What the case still needs. Ticked items are matched; the rest is what is left to do.
 *
 * Anything a case handler has additionally asked for sits directly underneath, in the same card and
 * deliberately not in the same list: the checklist is what the case status is derived from, and a
 * request is a question that does not move the case either way.
 */
function Checklist({ chosen }: { chosen: CaseOverview }) {
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
        <CaseScreen
          detail={open}
          onReview={review}
          onCaseChanged={() => show(open.overview.id)}
          error={error}
        />
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
        {cases.length === 0 && (
          <p className="empty">No cases yet. They appear here once someone opens one on the intake side.</p>
        )}
        {cases.map((c) => (
          <button key={c.id} className="case-row" onClick={() => void show(c.id)} disabled={opening !== null}>
            <span className="reference">
              {c.typeLabel}
              <span className="case-reference"> · {c.reference}</span>
            </span>
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
  onCaseChanged,
  error,
}: {
  detail: CaseDetail
  onReview: (documentId: string, caseId: string) => Promise<void>
  onCaseChanged: () => Promise<void>
  error: string | null
}) {
  const { overview } = detail

  return (
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
              standing={standingOf(doc, detail)}
              blocking={detail.blockedDocumentIds.includes(doc.id)}
              screening={detail.screenings.find((s) => s.documentId === doc.id)}
              onReview={() => void onReview(doc.id, doc.caseId)}
            />
          ))}
        </section>
      </div>

      <CaseChat detail={detail} onCaseChanged={onCaseChanged} />
    </div>
  )
}

/* --- the case chat -------------------------------------------------------- */

/**
 * A conversation about the one case beside it, so an answer and the document it came from are on
 * screen together.
 *
 * The turns and the proposals are seeded from the case detail — both are kept server-side, so a
 * handler who goes back to the list and returns does not lose the conversation. Local state carries
 * the turn they are typing now; re-opening the case resyncs from what came back.
 */
function CaseChat({ detail, onCaseChanged }: { detail: CaseDetail; onCaseChanged: () => Promise<void> }) {
  const [turns, setTurns] = useState<ChatTurn[]>(detail.conversation)
  const [proposals, setProposals] = useState<ProposalCard[]>(detail.proposals)
  const [question, setQuestion] = useState('')
  const [thinking, setThinking] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    setTurns(detail.conversation)
    setProposals(detail.proposals)
  }, [detail])

  async function ask(asked: string) {
    if (!asked.trim() || thinking) return
    setError(null)
    setThinking(true)
    setQuestion('')
    try {
      const answered = await askCaseChat(detail.overview.id, asked)
      setTurns((current) => [...current, answered.turn])
      setProposals(answered.proposals)
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setThinking(false)
    }
  }

  // A confirmed review moves the case, so the screen beside this one has to be read again. Nothing
  // else a card can do changes anything the case screen shows.
  async function resolve(proposal: ProposalCard, confirmed: boolean) {
    setError(null)
    try {
      const resolved = confirmed ? await confirmProposal(proposal.id) : await declineProposal(proposal.id)
      setProposals((current) => current.map((p) => (p.id === resolved.id ? resolved : p)))
      if (confirmed && resolved.kind === 'REVIEW') await onCaseChanged()
    } catch (e) {
      setError((e as Error).message)
    }
  }

  return (
    <aside className="chat">
      <h2>Ask about this case</h2>

      <div className="turns">
        {turns.length === 0 && !thinking && (
          <div className="suggestions">
            <p className="empty">Nothing asked yet.</p>
            {SUGGESTED_QUESTIONS.map((suggested) => (
              <button key={suggested} className="chip" onClick={() => void ask(suggested)}>
                {suggested}
              </button>
            ))}
          </div>
        )}

        {turns.map((turn, index) => (
          <Turn
            key={index}
            turn={turn}
            proposals={proposals.filter((p) => turn.proposalIds.includes(p.id))}
            onResolve={resolve}
          />
        ))}

        {thinking && (
          <p className="reading">
            <span className="spinner" aria-hidden />
            Reading the case…
          </p>
        )}
      </div>

      {error && (
        <p className="error" role="alert">
          {error}
        </p>
      )}

      <form
        className="asking"
        onSubmit={(e) => {
          e.preventDefault()
          void ask(question)
        }}
      >
        <input
          value={question}
          disabled={thinking}
          placeholder="What is the total on the receipt?"
          onChange={(e) => setQuestion(e.target.value)}
        />
        <button type="submit" disabled={thinking || !question.trim()}>
          Ask
        </button>
      </form>
    </aside>
  )
}

function Turn({
  turn,
  proposals,
  onResolve,
}: {
  turn: ChatTurn
  proposals: ProposalCard[]
  onResolve: (proposal: ProposalCard, confirmed: boolean) => Promise<void>
}) {
  return (
    <div className="turn">
      <p className="asked">{turn.question}</p>
      <p className="answered">{turn.answer}</p>

      {turn.toolCalls.length > 0 && (
        <ul className="tools">
          {turn.toolCalls.map((call, index) => (
            <li key={index}>{toolLabel(call)}</li>
          ))}
        </ul>
      )}

      {proposals.map((proposal) => (
        <ProposalCardView key={proposal.id} proposal={proposal} onResolve={onResolve} />
      ))}
    </div>
  )
}

/** A suggestion, and the two buttons that are the only way anything it suggests ever happens. */
function ProposalCardView({
  proposal,
  onResolve,
}: {
  proposal: ProposalCard
  onResolve: (proposal: ProposalCard, confirmed: boolean) => Promise<void>
}) {
  const what =
    proposal.kind === 'REVIEW'
      ? `Review ${proposal.subject} — let the case proceed despite its quality`
      : `Ask the claimant for ${proposal.subject}`

  return (
    <div className={`proposal ${proposal.state.toLowerCase()}`}>
      <strong>{what}</strong>
      <p>{proposal.reason}</p>

      {proposal.state === 'PROPOSED' ? (
        <div className="decide">
          <button className="confirm" onClick={() => void onResolve(proposal, true)}>
            Confirm
          </button>
          <button className="decline" onClick={() => void onResolve(proposal, false)}>
            Decline
          </button>
        </div>
      ) : (
        <p className="decided">{proposal.state === 'CONFIRMED' ? 'Confirmed by you.' : 'Declined by you.'}</p>
      )}
    </div>
  )
}

/**
 * One tool call, in words. The first argument is the document or label the call was about, which is
 * the part a handler needs in order to go and check the answer against the artefact.
 */
function toolLabel(call: ToolCall): string {
  const name = TOOL_LABEL[call.name] ?? call.name
  try {
    const subject = Object.values(JSON.parse(call.arguments) as Record<string, unknown>)
      .map(String)
      .find((value) => value.length > 0)
    return subject ? `${name} ${subject}` : name
  } catch {
    return name
  }
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
