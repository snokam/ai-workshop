import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { createClaim, streamHelp } from '../../api'
import { Failure } from '../../components/feedback/Failure'
import { Loader } from '../../components/feedback/Loader'
import { TaskGate } from '../../components/workshop/TaskGate'
import { openCreated } from './openedClaims'

/** Long enough that help does not chase every keystroke, short enough to arrive while still typing. */
const SETTLE_MS = 700

/** Below this there is nothing to be helpful about yet. */
const ENOUGH_TO_READ = 25

export function ReportWithHelp() {
  const navigate = useNavigate()
  const [description, setDescription] = useState('')
  const [help, setHelp] = useState('')
  const [helping, setHelping] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<Error | null>(null)
  const inFlight = useRef<AbortController | null>(null)

  // The form is the form. This only watches it: when typing settles, whatever is in the box is sent
  // for help, and the answer is written in a word at a time beside it. Nobody is waiting on it —
  // that is the whole reason it is worth streaming.
  useEffect(() => {
    const text = description.trim()
    if (text.length < ENOUGH_TO_READ) {
      setHelp('')
      return
    }
    const timer = setTimeout(() => {
      inFlight.current?.abort()
      const controller = new AbortController()
      inFlight.current = controller
      setHelp('')
      setHelping(true)
      void streamHelp(text, (token) => setHelp((soFar) => soFar + token), controller.signal)
        .catch(() => undefined)
        .finally(() => setHelping(false))
    }, SETTLE_MS)
    return () => clearTimeout(timer)
  }, [description])

  async function submit() {
    const text = description.trim()
    if (!text || submitting) return
    setSubmitting(true)
    setError(null)
    try {
      openCreated(navigate, await createClaim(text))
    } catch (e) {
      setError(e as Error)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <>
      <header>
        <h1>Report a claim, with help</h1>
        <p>
          The same form as the quick report. The difference is that something reads what you are
          writing as you write it, and says what would be worth adding before you send it.
        </p>
      </header>

      <TaskGate
        task="STREAMING_FORM_HELP"
        instead="Nothing reads the box while you type yet — the help that appears beside it has not been written."
      >
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
            placeholder="For example: our flight was cancelled and we had to book a hotel for the night."
            rows={7}
            disabled={submitting}
          />

          {(help || helping) && (
            <aside className="typing-help" aria-live="polite">
              <span className="typing-help-who">While you write</span>
              {help || <Loader />}
            </aside>
          )}

          <button type="submit" disabled={submitting || description.trim().length === 0}>
            {submitting ? (
              <span className="reading">
                <Loader />
                Reading…
              </span>
            ) : (
              'Open the claim'
            )}
          </button>
        </form>
      </TaskGate>

      {error && <Failure error={error} />}
    </>
  )
}
