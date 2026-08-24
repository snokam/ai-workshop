import { useEffect, useState } from 'react'
import { askCaseChat, confirmProposal, declineProposal } from '../../../api'
import type { ClaimDetail, ChatTurn, ProposalCard } from '../../../api'
import { SUGGESTED_QUESTIONS } from '../../../lib/labels'
import { Turn } from '../Turn'
import { Loader } from '../../feedback/Loader'
import { Failure } from '../../feedback/Failure'
import { TaskGate } from '../../workshop/TaskGate'

export function ClaimChat({
  detail,
  onCaseChanged,
}: {
  detail: ClaimDetail
  onCaseChanged: () => Promise<void>
}) {
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

  async function resolve(proposal: ProposalCard, confirmed: boolean) {
    setError(null)
    try {
      const resolved = confirmed
        ? await confirmProposal(proposal.id)
        : await declineProposal(proposal.id)
      setProposals((current) =>
        current.map((p) => (p.id === resolved.id ? resolved : p)),
      )
      if (confirmed && resolved.kind === 'REVIEW') await onCaseChanged()
    } catch (e) {
      setError(e as Error)
    }
  }

  return (
    <aside className="chat">
      <h2>Ask about this claim</h2>

      <TaskGate
        task="ADVISOR_CHAT"
        instead="Asking about a claim is a conversation with tools and a memory, and that agent has not been written yet."
      >
        <div className="turns">
          {turns.length === 0 && !thinking && (
            <div className="suggestions">
              <p className="empty">Nothing asked yet.</p>
              {SUGGESTED_QUESTIONS.map((suggested) => (
                <button
                  key={suggested}
                  className="chip"
                  onClick={() => void ask(suggested)}
                >
                  {suggested}
                </button>
              ))}
            </div>
          )}

          {turns.map((turn, index) => (
            <Turn
              key={index}
              turn={turn}
              proposals={proposals.filter((p) =>
                turn.proposalIds.includes(p.id),
              )}
              onResolve={resolve}
            />
          ))}

          {thinking && (
            <p className="reading">
              <Loader />
              Reading the claim…
            </p>
          )}
        </div>

        {error && <Failure error={error} />}

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
      </TaskGate>
    </aside>
  )
}
