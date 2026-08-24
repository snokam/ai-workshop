
const MY_CLAIMS_KEY = 'myClaimIds'

export function rememberedClaimIds(): string[] {
  try {
    const stored = JSON.parse(localStorage.getItem(MY_CLAIMS_KEY) ?? '[]')
    return Array.isArray(stored) ? (stored as string[]) : []
  } catch {
    return []
  }
}

export function rememberClaim(id: string) {
  const ids = rememberedClaimIds()
  if (!ids.includes(id)) localStorage.setItem(MY_CLAIMS_KEY, JSON.stringify([id, ...ids]))
}
