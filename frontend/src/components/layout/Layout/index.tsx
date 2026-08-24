import Snokam, { LayoutTheme, Padding, Width } from '@snokam/ui/layout'
import Theme from '@snokam/ui/theme'
import { FrameworkProvider } from '@snokam/ui/framework'
import { SimpleHeaderBase, HeaderTheme } from '@snokam/navbar/v1/base'
import { FooterBase, FooterTheme } from '@snokam/footer/v1/base'
import type { ReactNode } from 'react'
import { useLocation } from 'react-router-dom'
import { framework } from '../framework'
import { useDarkMode } from '../useDarkMode'

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
  const logo = {
    url: useDarkMode() ? '/snokam-logo-white.svg' : '/snokam-logo.svg',
    alt: 'Snøkam',
  }

  return (
    <Theme.Provider>
      <FrameworkProvider framework={framework}>
        <div className="page">
          <SimpleHeaderBase
            theme={HeaderTheme.Light}
            logo={logo}
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
            logo={logo}
            content={
              <p>
                A workshop on document handling with LLMs. Everything you upload
                stays on this machine. Each link says which tasks it exercises.
              </p>
            }
            links={[
              { href: '/', text: 'Tasks 1–2 · Report a case' },
              { href: '/cases', text: 'Tasks 1, 3 · My cases' },
              { href: '/casehandler', text: 'Tasks 3, 5–7 · Case handler' },
              { href: '/chat', text: 'Task 8 · Report with AI chat' },
            ]}
          />
        </div>
      </FrameworkProvider>
    </Theme.Provider>
  )
}
