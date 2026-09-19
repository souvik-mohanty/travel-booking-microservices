import { useEffect, useState } from 'react'
import { startGoogleLogin } from '@/features/auth/api'

// Google sign-in is a full-page navigation, so the page just sits there until
// the (possibly cold-starting) backend answers. `redirecting` lets the caller
// show a loading screen for that whole wait.
export function useGoogleLogin() {
  const [redirecting, setRedirecting] = useState(false)

  // Coming back with the browser's Back button restores this page from the
  // back/forward cache with its old state -- don't leave the overlay stuck on.
  useEffect(() => {
    function onPageShow(event: PageTransitionEvent) {
      if (event.persisted) setRedirecting(false)
    }
    window.addEventListener('pageshow', onPageShow)
    return () => window.removeEventListener('pageshow', onPageShow)
  }, [])

  function start() {
    setRedirecting(true)
    // Start the navigation only after the overlay has actually painted: once a
    // cross-origin navigation is pending the browser may stop presenting new
    // frames, so an overlay queued at the same moment can be missed entirely.
    requestAnimationFrame(() => setTimeout(startGoogleLogin, 0))
  }

  return { redirecting, start }
}
