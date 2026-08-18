import Theme from '@snokam/ui/theme'
import { FrameworkProvider } from '@snokam/ui/framework'
import { SimpleHeaderBase, HeaderTheme } from '@snokam/navbar/v1/base'
import { FooterBase, FooterTheme } from '@snokam/footer/v1/base'
import type { ReactNode } from 'react'
import { framework } from './framework'

/**
 * The real @snokam/navbar and @snokam/footer, in an app that has none of what they normally read.
 *
 * Their wired versions — SimpleHeader and Footer — pull the logo and every label out of Sanity, the
 * signed-in user out of next-auth and the link targets out of the tenant config. This app has no
 * CMS, no login and no tenant, so those would render empty strings, and useConfig would throw
 * before that. The Base components take the same values as props, which is all this needs.
 */
export function Layout({ children }: { children: ReactNode }) {
  return (
    <Theme.Provider>
      <FrameworkProvider framework={framework}>
        <SimpleHeaderBase
          theme={HeaderTheme.Light}
          logo={{ url: '/snokam-logo.svg', alt: 'Snøkam' }}
          homeUrl="/"
        />
        <main>{children}</main>
        <FooterBase
          theme={FooterTheme.Light}
          logo={{ url: '/snokam-logo.svg', alt: 'Snøkam' }}
          content={
            <p>
              A workshop on document handling with LLMs. Everything you upload stays on this
              machine.
            </p>
          }
          links={[
            { href: '/', text: 'Report a case' },
            { href: '/cases', text: 'My cases' },
            { href: '/casehandler', text: 'Case handler' },
          ]}
        />
      </FrameworkProvider>
    </Theme.Provider>
  )
}
