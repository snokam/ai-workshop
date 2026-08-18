import { useState } from 'react'
import { useNavigate, type NavigateFunction } from 'react-router-dom'
import { createCase, listCaseTypes } from '../api'
import type { CreatedCase, SupportedCaseType } from '../api'
import { rememberCase } from '../lib/myCases'
import { useEffect } from 'react'
import { Loader } from '../components/Loader'

/**
 * A new case has its own address the moment it exists, and the classifier's reasoning travels to it
 * in history state rather than in the URL — it is worth showing once, on arrival, and not worth
 * putting in a link somebody might send to someone else.
 */
function openCreated(navigate: NavigateFunction, created: CreatedCase) {
  rememberCase(created.id)
  navigate(`/cases/${created.id}`, { state: { intro: created } })
}

export function NewCase() {
  const navigate = useNavigate()
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
      openCreated(navigate, await createCase(text))
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <>
      <header>
        <h1>Report a case</h1>
        <p>
          Describe what happened in your own words. An agent reads it, opens the right kind of case
          for you, and tells you which documents you will need to send in.
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
              <Loader />
              Opening your case…
            </span>
          ) : (
            'Open the case'
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
