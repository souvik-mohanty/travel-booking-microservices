import { useEffect, useRef, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { LoadingOverlay } from '@/components/common/LoadingOverlay'
import { useAuthStore } from '@/store/authStore'

// Landing point for app.oauth2.authorized-redirect-uri in identity-service.
// OAuth2AuthenticationSuccessHandler appends accessToken/refreshToken as query
// params; OAuth2AuthenticationFailureHandler appends an `error` param instead.

interface SignInFailure {
  message: string
  // The raw reason from the backend (e.g. "[authorization_request_not_found]"),
  // kept visible because "sign-in failed" alone gives nobody anything to act on.
  detail?: string
}

// Only translates reasons a visitor can act on; everything else keeps the
// generic message and shows the raw reason underneath.
function describeFailure(oauthError: string): SignInFailure {
  if (oauthError.includes('authorization_request_not_found') || oauthError.includes('invalid_state')) {
    return {
      message: 'Your sign-in session expired before Google sent you back. Please try again.',
      detail: oauthError,
    }
  }
  if (oauthError.includes('access_denied')) {
    return { message: 'Google sign-in was cancelled or this account is not allowed.', detail: oauthError }
  }
  return { message: 'Google sign-in failed. Please try again.', detail: oauthError }
}

export function OAuth2RedirectPage() {
  const [searchParams] = useSearchParams()
  const setSession = useAuthStore((state) => state.setSession)
  const navigate = useNavigate()
  const [failure, setFailure] = useState<SignInFailure | null>(null)
  const handled = useRef(false)

  useEffect(() => {
    if (handled.current) return
    handled.current = true

    const accessToken = searchParams.get('accessToken')
    const refreshToken = searchParams.get('refreshToken')
    const oauthError = searchParams.get('error')

    if (oauthError) {
      setFailure(describeFailure(oauthError))
      return
    }

    if (!accessToken || !refreshToken) {
      setFailure({ message: 'The sign-in redirect did not include your session. Please try again.' })
      return
    }

    setSession({ accessToken, refreshToken })
    navigate('/tours', { replace: true })
  }, [setSession, navigate, searchParams])

  if (failure) {
    return (
      <div className="flex min-h-screen flex-col items-center justify-center gap-4 bg-slate-50 px-6 text-center">
        <p className="text-sm text-red-600">{failure.message}</p>
        {failure.detail && (
          <p className="max-w-md break-words rounded-md bg-slate-100 px-3 py-2 font-mono text-xs text-slate-500">
            {failure.detail}
          </p>
        )}
        <button
          onClick={() => navigate('/login', { replace: true })}
          className="text-sm font-medium text-blue-600 underline decoration-blue-300 underline-offset-2 transition-colors duration-150 hover:text-blue-700"
        >
          Back to sign in
        </button>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-slate-50">
      <LoadingOverlay open title={'Signing you in…'} message="Finishing your Google sign-in." />
    </div>
  )
}
