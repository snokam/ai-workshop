import { LoaderVariant, LogoLoader, PageLoader } from '@snokam/ui/loader'
import type { ReactNode } from 'react'

export function Loader({ size = 18 }: { size?: number }) {
  return <LogoLoader variant={LoaderVariant.Pulse} height={size} aria-hidden />
}

export function PageWait({ children }: { children?: ReactNode }) {
  return (
    <div className="page-wait" role="status">
      <PageLoader variant={LoaderVariant.Pulse} height={56} aria-hidden />
      {children && <p>{children}</p>}
    </div>
  )
}
