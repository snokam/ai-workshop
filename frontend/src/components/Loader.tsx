import { LoaderVariant, LogoLoader, PageLoader } from '@snokam/ui/loader'
import type { ReactNode } from 'react'

/**
 * The snokam logo loader, sized to sit on a line of text.
 *
 * Most waiting states here are a sentence saying what is being waited on, so the mark goes beside
 * the words rather than replacing them, and is hidden from screen readers — the sentence says it.
 */
export function Loader({ size = 18 }: { size?: number }) {
  return <LogoLoader variant={LoaderVariant.Pulse} height={size} aria-hidden />
}

/**
 * The whole-screen version, for when there is nothing to show yet rather than something being
 * added to what is already there. PageLoader centres the mark and claims the space it is given.
 */
export function PageWait({ children }: { children?: ReactNode }) {
  return (
    <div className="page-wait" role="status">
      <PageLoader variant={LoaderVariant.Pulse} height={56} aria-hidden />
      {children && <p>{children}</p>}
    </div>
  )
}
