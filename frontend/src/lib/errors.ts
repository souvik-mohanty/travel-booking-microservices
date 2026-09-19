import { isAxiosError } from 'axios'

// Fields Spring adds to its default error body -- never useful to show a person.
const SPRING_ERROR_FIELDS = new Set(['timestamp', 'status', 'error', 'path', 'trace'])

// Turns whatever the backend sent back into one readable sentence: a plain-text
// body, a { message }, or a { field: "reason" } validation map.
export function apiErrorMessage(error: unknown, fallback: string): string {
  if (!isAxiosError(error)) return fallback

  const data: unknown = error.response?.data
  if (typeof data === 'string' && data.trim() && !data.trimStart().startsWith('<')) return data

  if (data && typeof data === 'object') {
    const body = data as Record<string, unknown>
    if (typeof body.message === 'string' && body.message.trim()) return body.message
    for (const [key, value] of Object.entries(body)) {
      if (!SPRING_ERROR_FIELDS.has(key) && typeof value === 'string' && value.trim()) return value
    }
  }

  if (error.response?.status === 403) return 'You do not have permission to do that.'
  if (error.response?.status === 404) return 'That item no longer exists.'
  return fallback
}

// Optional form fields: an empty input means "not provided", not "".
export function blankToUndefined(value: string | undefined): string | undefined {
  const trimmed = value?.trim()
  return trimmed ? trimmed : undefined
}
