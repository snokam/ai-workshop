import { useEffect, useState } from 'react'
import { askCaseChat, confirmProposal, declineProposal } from '../api'
import type { CaseDetail, ChatTurn, ProposalCard } from '../api'
import { SUGGESTED_QUESTIONS } from '../lib/labels'
import { Turn } from './Turn'
import { Loader } from './Loader'
import { Failure } from './Failure'

export function CaseChat({ detail, onCaseChanged }: { detail: CaseDetail; onCaseChanged: () => Promise<void> }) {
  const [turns, setTurns] = useState<ChatTurn[]>(detail.conversation)
  const [proposals, setProposals] = useState<ProposalCard[]>(detail.proposals)
  const [question, setQuestion] = useState('')
  const [thinking, setThinking] = useState(false)
  const [error, setError] = useState<Error | null>(null)

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
      setError(e as Error)
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
      setError(e as Error)
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
            <Loader />
            Reading the case…
          </p>
        )}
      </div>

      {error && (
        <Failure error={error} />
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
