import type { ChatTurn, ProposalCard } from '../../../api'
import { toolLabel } from '../../../lib/labels'
import { ProposalCardView } from '../ProposalCard'

export function Turn({
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
