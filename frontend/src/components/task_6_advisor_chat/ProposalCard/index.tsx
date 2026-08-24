import type { ProposalCard } from '../../../api'

export function ProposalCardView({
  proposal,
  onResolve,
}: {
  proposal: ProposalCard
  onResolve: (proposal: ProposalCard, confirmed: boolean) => Promise<void>
}) {
  const what =
    proposal.kind === 'REVIEW'
      ? `Review ${proposal.subject} — let the claim proceed despite its quality`
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
