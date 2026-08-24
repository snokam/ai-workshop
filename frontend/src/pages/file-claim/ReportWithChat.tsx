import { useState } from 'react'
import { useNavigate, type NavigateFunction } from 'react-router-dom'
import { interviewIntake } from '../../api'
import type { CreatedClaim, InterviewAnswer } from '../../api'
import { rememberClaim } from './openedClaims'
import { Loader } from '../../components/feedback/Loader'
import { Failure } from '../../components/feedback/Failure'
import { TaskGate } from '../../components/workshop/TaskGate'

function openCreated(navigate: NavigateFunction, created: CreatedClaim) {
  rememberClaim(created.id)
  navigate(`/claims/${created.id}`, { state: { intro: created } })
}

export function ReportWithChat() {
  const navigate = useNavigate()
  const [description, setDescription] = useState('')
  const [answered, setAnswered] = useState<InterviewAnswer[]>([])
  const [questions, setQuestions] = useState<string[]>([])
  const [replies, setReplies] = useState<Record<number, string>>({})
  const [rationale, setRationale] = useState<string | null>(null)
  const [started, setStarted] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<Error | null>(null)

  // The whole conversation is resent each turn, so the agent stays stateless and the page owns the
  // history: on a decision the claim is already open, so we jump straight to it.
  async function send(nextAnswers: InterviewAnswer[]) {
    setBusy(true)
    setError(null)
    try {
      const response = await interviewIntake(description.trim(), nextAnswers)
      if (response.status === 'DECIDED' && response.createdClaim) {
        openCreated(navigate, response.createdClaim)
        return
      }
      setAnswered(nextAnswers)
      setQuestions(response.questions)
      setReplies({})
      setRationale(response.rationale)
      setStarted(true)
    } catch (e) {
      setError(e as Error)
    } finally {
      setBusy(false)
    }
  }

  function start() {
    if (!description.trim() || busy) return
    void send([])
  }

  function answer() {
    const additions = questions
      .map((question, i) => ({ question, answer: (replies[i] ?? '').trim() }))
      .filter((qa) => qa.answer.length > 0)
    if (additions.length === 0 || busy) return
    void send([...answered, ...additions])
  }

  const allAnswered =
    questions.length > 0 && questions.every((_, i) => (replies[i] ?? '').trim().length > 0)

  return (
    <>
      <header>
        <h1>Report with AI chat</h1>
        <p>
          Tell us what happened. Unlike the quick report, the agent may ask a
          couple of follow-up questions first, so it can open exactly the right
          claim and ask only for the documents that situation needs.
        </p>
      </header>

      <TaskGate
        task="CREATE_CLAIM_CHAT"
        instead="Holding a short conversation is how this claim gets opened, and the agent that runs it has not been written yet."
      >
        <div className="intake-chat">
          {started && (
            <div className="chat-log">
              <p className="chat-said">
                <span className="chat-who">You</span>
                {description.trim()}
              </p>
              {answered.map((qa, i) => (
                <div key={i} className="chat-exchange">
                  <p className="chat-asked">
                    <span className="chat-who">Agent</span>
                    {qa.question}
                  </p>
                  <p className="chat-said">
                    <span className="chat-who">You</span>
                    {qa.answer}
                  </p>
                </div>
              ))}
            </div>
          )}

          {!started ? (
            <form
              className="describe"
              onSubmit={(e) => {
                e.preventDefault()
                start()
              }}
            >
              <textarea
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="For example: our trip fell through and I want to know what I can claim."
                rows={5}
                disabled={busy}
              />
              <button type="submit" disabled={busy || description.trim().length === 0}>
                {busy ? (
                  <span className="reading">
                    <Loader />
                    Reading…
                  </span>
                ) : (
                  'Start the chat'
                )}
              </button>
            </form>
          ) : questions.length > 0 ? (
            <form
              className="chat-questions"
              onSubmit={(e) => {
                e.preventDefault()
                answer()
              }}
            >
              {rationale && <p className="chat-note">{rationale}</p>}
              {questions.map((question, i) => (
                <label key={i} className="chat-question">
                  <span>{question}</span>
                  <input
                    type="text"
                    value={replies[i] ?? ''}
                    onChange={(e) => setReplies((r) => ({ ...r, [i]: e.target.value }))}
                    disabled={busy}
                    autoFocus={i === 0}
                  />
                </label>
              ))}
              <button type="submit" disabled={busy || !allAnswered}>
                {busy ? (
                  <span className="reading">
                    <Loader />
                    Reading…
                  </span>
                ) : (
                  'Send answers'
                )}
              </button>
            </form>
          ) : (
            busy && (
              <p className="reading">
                <Loader />
                Opening your claim…
              </p>
            )
          )}
        </div>
      </TaskGate>

      {error && <Failure error={error} />}
    </>
  )
}
