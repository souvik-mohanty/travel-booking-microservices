import { API_BASE_URL } from '@/lib/apiClient'

// Free-tier hosting (Render) puts every service to sleep after 15 minutes
// idle. Left alone, each one wakes up only when the user first reaches for its
// feature -- so a session would hit several cold starts one after another.
// Pinging them all at once on page load wakes them in parallel instead, so
// they're mostly ready by the time the user clicks anything.
//
// VITE_WARMUP_URLS is a comma-separated list of the services' PUBLIC base URLs
// (unset locally, where nothing sleeps). no-cors + ignored result: only the
// wake-up matters, and the actuator endpoint sends no CORS headers.
export function warmUpBackend(): void {
  const configured = (import.meta.env.VITE_WARMUP_URLS ?? '')
    .split(',')
    .map((url) => url.trim().replace(/\/$/, ''))
    .filter(Boolean)

  if (configured.length === 0) return

  const targets = new Set([API_BASE_URL.replace(/\/$/, ''), ...configured])
  for (const base of targets) {
    fetch(`${base}/actuator/health`, { mode: 'no-cors', cache: 'no-store' }).catch(() => {})
  }
}
