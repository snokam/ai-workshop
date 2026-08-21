import { useSyncExternalStore } from 'react'

/*
 * The header and footer come from @snokam/navbar and @snokam/footer, which colour themselves from
 * --color-light-blue / --color-primary. Those variables are only injected by the ColorSchemeProvider
 * this app does not mount, so both bars end up transparent and the page background shows through.
 * In dark mode that background is navy, and so is the logo, so the logo all but disappears — hence
 * the swap to the white file.
 */
const DARK = '(prefers-color-scheme: dark)'

function subscribe(onChange: () => void) {
  const media = window.matchMedia(DARK)
  media.addEventListener('change', onChange)
  return () => media.removeEventListener('change', onChange)
}

export function useDarkMode(): boolean {
  return useSyncExternalStore(
    subscribe,
    () => window.matchMedia(DARK).matches,
    () => false,
  )
}
