
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
