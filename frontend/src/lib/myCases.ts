/**
 * There is no login, so "my cases" cannot mean cases belonging to an account. It means the ones
 * opened from this browser: their ids are remembered here and matched against what the backend still
 * has, so a restart (the store is in-memory) quietly empties the list rather than showing stale rows.
 */

const MY_CASES_KEY = 'myCaseIds'

export function rememberedCaseIds(): string[] {
  try {
    const stored = JSON.parse(localStorage.getItem(MY_CASES_KEY) ?? '[]')
    return Array.isArray(stored) ? (stored as string[]) : []
  } catch {
    return []
  }
}

export function rememberCase(id: string) {
  const ids = rememberedCaseIds()
  if (!ids.includes(id)) localStorage.setItem(MY_CASES_KEY, JSON.stringify([id, ...ids]))
}

/** What the claimant is looking at: the two entry points, or one case opened for upload. */
