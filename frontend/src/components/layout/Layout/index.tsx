import Snokam, { LayoutTheme, Padding, Width } from '@snokam/ui/layout'
import Theme from '@snokam/ui/theme'
import { FrameworkProvider } from '@snokam/ui/framework'
import { SimpleHeaderBase, HeaderTheme } from '@snokam/navbar/v1/base'
import { FooterBase, FooterTheme } from '@snokam/footer/v1/base'
import type { ReactNode } from 'react'
import { useLocation } from 'react-router-dom'
import { framework } from '../framework'

function backFrom(pathname: string): { url: string; text: string } | null {
  if (/^\/casehandler\/cases\/[^/]+$/.test(pathname)) {
    return { url: '/casehandler', text: 'All cases' }
  }
  if (/^\/cases\/[^/]+$/.test(pathname)) {
    return { url: '/cases', text: 'My cases' }
  }
  return null
}

export function Layout({ children }: { children: ReactNode }) {
  const back = backFrom(useLocation().pathname)

  return (
    <Theme.Provider>
      <FrameworkProvider framework={framework}>
        <div className="page">
          <SimpleHeaderBase
            theme={HeaderTheme.Light}
            logo={{ url: '/snokam-logo.svg', alt: 'Snøkam' }}
            homeUrl="/"
            backUrl={back?.url}
            backText={back?.text}
          />
          <Snokam.Container as="main" theme={LayoutTheme.Light}>
            <Snokam.Content width={Width.Normal}>
              <Snokam.Section padding={Padding.Large} stretchItems>
                {children}
              </Snokam.Section>
            </Snokam.Content>
          </Snokam.Container>
          <FooterBase
            theme={FooterTheme.Light}
            logo={{ url: '/snokam-logo.svg', alt: 'Snøkam' }}
            content={
              <p>
                A workshop on document handling with LLMs. Everything you upload
                stays on this machine.
              </p>
            }
            links={[
              { href: '/', text: 'Report a case' },
              { href: '/cases', text: 'My cases' },
              { href: '/casehandler', text: 'Case handler' },
              { href: '/chat', text: 'Report with AI chat' },
            ]}
          />
        </div>
      </FrameworkProvider>
    </Theme.Provider>
  )
}
