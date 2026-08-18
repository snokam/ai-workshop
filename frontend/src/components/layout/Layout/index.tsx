import Snokam, { LayoutTheme, Padding, Width } from '@snokam/ui/layout'
import Theme from '@snokam/ui/theme'
import { FrameworkProvider } from '@snokam/ui/framework'
import { SimpleHeaderBase, HeaderTheme } from '@snokam/navbar/v1/base'
import { FooterBase, FooterTheme } from '@snokam/footer/v1/base'
import type { ReactNode } from 'react'
import { useLocation } from 'react-router-dom'
import { framework } from '../framework'

/**
 * The real @snokam/navbar and @snokam/footer, in an app that has none of what they normally read.
 *
 * Their wired versions — SimpleHeader and Footer — pull the logo and every label out of Sanity, the
 * signed-in user out of next-auth and the link targets out of the tenant config. This app has no
 * CMS, no login and no tenant, so those would render empty strings, and useConfig would throw
 * before that. The Base components take the same values as props, which is all this needs.
 *
 * The `page` wrapper is what holds the footer at the bottom: a column that is at least the height
 * of the viewport, with `main` taking the slack, so a short screen still puts the footer on the
 * fold rather than partway up an empty page.
 *
 * `main` is Snokam.Container/Content/Section, the same three the header and footer use, so the
 * page gutters and the maximum text width line up with the chrome above and below rather than
 * being a second set of numbers that happens to look close.
 */
/**
 * Where the header's back link points, worked out from the address. A case is always reached from
 * one list, so the way back is a fixed place rather than whatever the history stack happens to
 * hold — a reload or a pasted link then still has somewhere to go.
 */
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
            ]}
          />
        </div>
      </FrameworkProvider>
    </Theme.Provider>
  )
}
